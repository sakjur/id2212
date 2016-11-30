package is.mjuk.market.server;

import is.mjuk.market.common.Item;
import is.mjuk.market.common.Client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.Comparator;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ItemImpl extends UnicastRemoteObject implements Item, Comparator<Item> {
    private String name;
    private int price;
    private Client client;

    private ItemImpl() throws RemoteException {
    }

    public ItemImpl(String name, int price) throws RemoteException {
        this.name = name;
        this.price = price;
    }

    public ItemImpl(String name, int price, Client client) throws RemoteException {
        this.name = name;
        this.price = price;
        this.client = client;
    }

    public boolean store(Connection conn) {
        String insert = "INSERT INTO items "
            + "(name, price, owner, available) VALUES "
            + "(?, ?, ?, true)";
        String select = "SELECT id FROM users WHERE username = ? LIMIT 1";
        try {
            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setString(1, this.name);
            stmt.setInt(2, this.price);
            stmt.setString(3, this.client.getName());
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println(e);
            return false;
        }
        return true;
    }

    public int compare(Item a, Item b) {
        try {
            return a.getPrice() - b.getPrice();
        } catch (RemoteException e) {
            return 0;
        }
    }

    public boolean equals(Item i) {
        try {
            if (this.name == i.getName() && this.price == i.getPrice())
                return true;
        } catch (RemoteException e) {
        }
        return false;
    }

    public Client getOwner() {
        return this.client;
    }

    @Override
    public String getName() throws RemoteException {
        return this.name;
    }

    @Override
    public int getPrice() throws RemoteException {
        return this.price;
    }
}
