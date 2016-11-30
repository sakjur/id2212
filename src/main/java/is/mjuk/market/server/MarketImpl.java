package is.mjuk.market.server;

import is.mjuk.market.common.Market;
import is.mjuk.market.common.MarketObserver;
import is.mjuk.market.common.Client;
import is.mjuk.market.common.Item;
import is.mjuk.market.common.Session;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.Account;
import se.kth.id2212.ex2.bankrmi.RejectedException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.security.SecureRandom;
import java.math.BigInteger;

public class MarketImpl extends UnicastRemoteObject implements Market {
    private class Subscribe {
        public int price;
        public Client client;

        public Subscribe(int price, Client client) {
            this.price = price;
            this.client = client;
        }
    }

    private Map<String, MarketObserver> observers = new HashMap<>();
    private Map<String, ArrayList<Subscribe>> subs = new HashMap<>();
    private Map<String, String> sessions = new HashMap<>();

    private SecureRandom random = new SecureRandom();

    private Bank bank;

    private Connection conn = null;

    private MarketImpl() throws RemoteException {
    }

    private Connection getConnection() {
        if (conn == null) {
            try {
                conn = DriverManager.getConnection("jdbc:mysql://localhost/market?user=root");
            } catch (SQLException e) {
                conn = null;
            }
        }
        return conn;
    };

    public MarketImpl(Bank bank) throws RemoteException {
        super();
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception e) {
            System.err.println(e);
            System.exit(-4);
        }
        this.bank = bank;
    }

    @Override
    public String[] listItems() throws RemoteException {
        String select = "SELECT name FROM items WHERE available = true GROUP BY name";
        ArrayList<String> list = new ArrayList<String>();
        try {
            PreparedStatement stmt = conn.prepareStatement(select);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                list.add(rs.getString("name"));
            }

            String rv[] = new String[list.size()];
            if (list == null) {
                return new String[0];
            } else {
                return list.toArray(rv);
            }
        } catch (SQLException e) {
            System.err.println(e);
        }
        return new String[0];
    }

    @Override
    public Item addItem(String session, String name, int price) throws RemoteException {
        Client owner = this.getClient(session);
        ItemImpl item;
        item = new ItemImpl(name, price, owner);

        if (!item.store(this.getConnection())) {
            return null;
        }

        ArrayList<Subscribe> subscribers = this.subs.get(name);
        if (subscribers != null) {
            ArrayList<Subscribe> tmpSub = new ArrayList<Subscribe>();
            for (Subscribe s : subscribers) {
                if (s.price >= price) {
                    MarketObserver observer = this.observers.get(s.client.getName());
                    observer.notifySub(item);
                } else {
                    tmpSub.add(s);
                }
            }
            this.subs.put(name, tmpSub);
        }

        return (Item) item;
    }

    @Override
    public Item[] getItems(String name) throws RemoteException {
        ArrayList<Item> list = new ArrayList<Item>();
        String select = "SELECT name, price FROM items "
            + "WHERE name = ? AND available = true";
        try {
            PreparedStatement stmt = conn.prepareStatement(select);
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                list.add((Item) new ItemImpl(rs.getString("name"),
                                             rs.getInt("price")));
            }

            Item i[] = new Item[list.size()];
            if (list == null) {
                return null;
            } else {
                return list.toArray(i);
            }
        } catch (SQLException e) {
            System.err.println(e);
            return null;
        }
    }

    private ItemImpl internalGetItem(String name, int price) throws RemoteException {
        String select = "SELECT name, price, owner FROM items "
            + "WHERE name = ? AND price = ? AND available = true";
        try {
            PreparedStatement stmt = conn.prepareStatement(select);
            stmt.setString(1, name);
            stmt.setInt(2, price);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            if (name.equals(rs.getString("name")) && price == rs.getInt("price")) {
                return new ItemImpl(name, price, this.getClientFromName(rs.getString("owner")));
            } else {
                return null;
            }
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }

    @Override
    public Item getItem(String name, int price) throws RemoteException {
        return (Item) internalGetItem(name, price);
    }

    private synchronized ItemImpl internalDeleteItem(String name, int price)
        throws RemoteException {
        String select = "SELECT id, owner FROM items "
            + "WHERE available = true AND name = ? AND price = ? LIMIT 1";
        String update = "UPDATE items SET available = false WHERE id = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(select);
            PreparedStatement updt = conn.prepareStatement(update);
            stmt.setString(1, name);
            stmt.setInt(2, price);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            String owner = rs.getString("owner");
            updt.setInt(1, rs.getInt("id"));
            updt.executeUpdate();

            return new ItemImpl(name, price, this.getClientFromName(owner));
        } catch (SQLException e) {
            System.err.println(e);
        }
        return null;
    }

    @Override
    public synchronized Item deleteItem(String session, String name, int price) throws RemoteException {
        String user = this.getClient(session).getName();
        if (!this.internalGetItem(name, price).getOwner().getName().equals(user)) {
            return null;
        }
        return (Item) this.internalDeleteItem(name, price);
    }

    @Override
    public synchronized Item buyItem(String session, String name, int price)
        throws RemoteException {
        Client buyer = this.getClient(session);
        ItemImpl result = this.internalGetItem(name, price);
        if (result == null) {
            return null;
        } 
        
        Client client = result.getOwner();
        if (client == null) {
            System.err.println("Seller disappeared?");
            this.internalDeleteItem(name, price);
            return null;
        }
        Account withdrawAcc = buyer.getAccount();
        Account depositAcc = client.getAccount();
        if (depositAcc == null) {
            System.err.println("Seller disappeared??");
            this.internalDeleteItem(name, price);
            return null;
        }
        try {
            withdrawAcc.withdraw(price);
            depositAcc.deposit(price);
        } catch (RejectedException e) {
            // Don't sell the item
            return null;
        }
        result = this.internalDeleteItem(name, price);

        MarketObserver notify = this.observers.get(client.getName());
        if (notify != null) {
            notify.notifySold(result);
        }

        try {
            Connection conn = this.getConnection();
            PreparedStatement stmt;
            String increaseBuyer = "UPDATE users SET bought = bought + 1 "
                + "WHERE username = ?";
            String increaseSeller = "UPDATE users SET sold = sold + 1 "
                + "WHERE username = ?";

            stmt = conn.prepareStatement(increaseBuyer);
            stmt.setString(1, buyer.getName());
            stmt.executeUpdate();
            stmt = conn.prepareStatement(increaseSeller);
            stmt.setString(1, client.getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e);
        }
        return (Item) result;
    }

    @Override
    public String register(String name, String password) throws RemoteException {
        System.out.format("Adding user %s\n", name);
        Account acc = null;
        while (acc == null) {
            acc = bank.getAccount(name);
            try {
                acc = bank.newAccount(name);
            } catch (RejectedException e) {
            }
        }
        ClientImpl intClient = new ClientImpl(name, acc);
        if (!intClient.setPassword(password)) {
            return null;
        }
        intClient.store(this.getConnection());
        return login(name, password);
    }

    @Override
    public String login(String name, String password) throws RemoteException {
        String sessionKey = new BigInteger(130, random).toString(32);
        String getUser = "SELECT username FROM users "
            + "WHERE username = ? AND passwd = ? AND active = true LIMIT 1";
        Connection conn = this.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(getUser);
            stmt.setString(1, name);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            if (name.equals(rs.getString("username"))) {
                this.sessions.put(sessionKey, name);
                return sessionKey; 
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.err.println(e);
        }
        return null;
    }

    private Client getClientFromName(String name) throws RemoteException {
        String getUser = "SELECT username FROM users "
            + "WHERE username = ? AND active = true LIMIT 1";
        Account acc = bank.getAccount(name);
        Connection conn = this.getConnection();
        if (acc == null) {
            System.err.println("Account error");
            return null;
        }
        while (acc == null) {
            acc = bank.getAccount(name);
            try {
                acc = bank.newAccount(name);
            } catch (RejectedException e) {
            }
        }
        try {
            PreparedStatement stmt = conn.prepareStatement(getUser);
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                if (name.equals(rs.getString("username"))) {
                    return (Client) new ClientImpl(name, acc);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println(e);
            return null;
        }
        return null;
    }

    @Override
    public Client getClient(String session) throws RemoteException {
        String name = this.sessions.get(session);

        if (name == null) {
            return null;
        }
        
        return this.getClientFromName(name);
    }

    @Override
    public Client deleteClient(String session) throws RemoteException {
        Client client = this.getClient(session);
        System.out.format("Deleting user %s\n", client.getName());
        try {
            String deactivateUser = "UPDATE users SET active = false " 
                + "WHERE username = ?";

            PreparedStatement stmt = conn.prepareStatement(deactivateUser);
            stmt.setString(1, client.getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e);
        }
        return client; 
    }

    @Override
    public void registerObserver(String session, MarketObserver observer)
        throws RemoteException {
        Client client = this.getClient(session);
        this.observers.put(client.getName(), observer);
    }

    @Override
    public boolean deleteObserver(String session, MarketObserver observer)
        throws RemoteException {
        Client client = this.getClient(session);
        if (client == null) {
            return false;
        }
        MarketObserver tmp = this.observers.get(client.getName());
        if (tmp == null) {
            return false;
        }
        if (observer.getId() != tmp.getId()) {
            return false;
        }
        return (this.observers.remove(client.getName()) != null);
    }

    @Override
    public void deleteSession(String session) throws RemoteException {
        this.sessions.remove(session);
    }

    @Override
    public void addSub(String session, String name, int price) throws RemoteException {
        ArrayList<Subscribe> list = this.subs.get(name);
        Client client = this.getClient(session);
        if (list == null) {
            System.out.println("Creating new product");
            list = new ArrayList<Subscribe>();
            this.subs.put(name, list);
        }

        list.add(new Subscribe(price, client));
    }

    @Override
    public void deleteSub(String session, String name) throws RemoteException {
        ArrayList<Subscribe> list = this.subs.get(name);
        Client client = this.getClient(session);
        if (list == null) {
            return;  
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).client.getName().equals(client.getName())) {
                list.remove(i);
                return;
            }
        }
    }
}
