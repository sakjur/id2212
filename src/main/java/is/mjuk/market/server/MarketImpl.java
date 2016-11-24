package is.mjuk.market.server;

import is.mjuk.market.common.Market;
import is.mjuk.market.common.MarketObserver;
import is.mjuk.market.common.Client;
import is.mjuk.market.common.Item;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.Account;
import se.kth.id2212.ex2.bankrmi.RejectedException;

public class MarketImpl extends UnicastRemoteObject implements Market {
    private class Subscribe {
        public int price;
        public Client client;

        public Subscribe(int price, Client client) {
            this.price = price;
            this.client = client;
        }
    }

    private Map<String, Client> clients = new HashMap<>();
    private Map<String, MarketObserver> observers = new HashMap<>();
    private Map<String, ArrayList<ItemImpl>> items = new HashMap<>();
    private Map<String, ArrayList<Subscribe>> subs = new HashMap<>();

    private Bank bank;

    private MarketImpl() throws RemoteException {
    }

    public MarketImpl(Bank bank) throws RemoteException {
        super();
        this.bank = bank;
    }

    @Override
    public int countItems() throws RemoteException {
        return items.size();
    }

    @Override
    public String[] listItems() throws RemoteException {
        return items.keySet().toArray(new String[0]);
    }

    @Override
    public Item addItem(String name, int price, Client owner) throws RemoteException {
        ItemImpl item;
        item = new ItemImpl(name, price, owner);
        ArrayList<ItemImpl> list = this.items.get(name);
        if (list == null) {
            System.out.println("Creating new product");
            list = new ArrayList<ItemImpl>();
            this.items.put(name, list);
        }

        list.add(item);
        Collections.sort(list, item);

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
        ArrayList<ItemImpl> list = this.items.get(name);
        Item i[] = new Item[list.size()];
        if (list == null) {
            return null;
        } else {
            return list.toArray(i);
        }
    }

    private ItemImpl internalGetItem(String name, int price) throws RemoteException {
        ArrayList<ItemImpl> list = this.items.get(name);
        if (list == null || list.size() == 0) {
            return null;
        }
        ItemImpl tmpItem = new ItemImpl(name, price);
        int pos = Collections.binarySearch(list, tmpItem, tmpItem);
        if (pos < 0) {
            return null;
        } else {
            return list.get(pos);
        }
    }

    @Override
    public Item getItem(String name, int price) throws RemoteException {
        return (Item) internalGetItem(name, price);
    }

    private synchronized ItemImpl internalDeleteItem(String name, int price)
        throws RemoteException {
        ArrayList<ItemImpl> list = this.items.get(name);
        if (list == null) {
            return null;
        }
        ItemImpl tmpItem = new ItemImpl(name, price);
        int pos = Collections.binarySearch(list, tmpItem, tmpItem);
        if (pos < 0) {
            return null;
        }
        ItemImpl rv = list.remove(pos);
        if (list.size() == 0) {
            this.items.remove(name);
        }
        return rv;
    }

    @Override
    public synchronized Item deleteItem(String name, int price) throws RemoteException {
        return (Item) this.internalDeleteItem(name, price);
    }

    @Override
    public synchronized Item buyItem(String name, int price, Client buyer)
    throws RemoteException {
        ItemImpl result = this.internalGetItem(name, price);
        if (result == null) {
            return null;
        } 
        
        Client client = result.getOwner();
        Account withdrawAcc = buyer.getAccount();
        Account depositAcc = client.getAccount();
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
        return (Item) result;
    }

    @Override
    public Client addClient(String name) throws RemoteException {
        Client client;
        if ((client = this.clients.get(name)) != null) {
            System.err.println("User already exists");
        }  else {
            client = new ClientImpl(name, bank.getAccount(name));
            this.clients.put(name, client);
        }
        return client;
    }

    @Override
    public Client getClient(String name) throws RemoteException {
        return this.clients.get(name);
    }
    @Override
    public boolean deleteClient(Client client) throws RemoteException {
        System.out.format("Deleting user %s\n", client.getName());
        return (this.clients.remove(client.getName()) != null);
    }

    @Override
    public void registerObserver(Client client, MarketObserver observer)
        throws RemoteException {
        this.observers.put(client.getName(), observer);
    }

    @Override
    public boolean deleteObserver(Client client, MarketObserver observer)
        throws RemoteException {
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
    public void addSub(String name, int price, Client client) throws RemoteException {
        ArrayList<Subscribe> list = this.subs.get(name);
        if (list == null) {
            System.out.println("Creating new product");
            list = new ArrayList<Subscribe>();
            this.subs.put(name, list);
        }

        list.add(new Subscribe(price, client));
    }

    @Override
    public void deleteSub(String name, Client client) throws RemoteException {
        ArrayList<Subscribe> list = this.subs.get(name);
        if (list == null) {
            return;  
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).client.getName() == client.getName()) {
                list.remove(i);
                return;
            }
        }
    }
}
