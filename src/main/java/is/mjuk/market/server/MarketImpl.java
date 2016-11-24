package is.mjuk.market.server;

import is.mjuk.market.common.Market;
import is.mjuk.market.common.Client;
import is.mjuk.market.common.Item;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MarketImpl extends UnicastRemoteObject implements Market {
    private Map<String, Client> clients = new HashMap<>();
    private Map<String, ArrayList<ItemImpl>> items = new HashMap<>();

    public MarketImpl() throws RemoteException {
        super();
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

    @Override
    public Item getItem(String name, int price) throws RemoteException {
        ArrayList<ItemImpl> list = this.items.get(name);
        if (list == null || list.size() == 0) {
            return null;
        }
        ItemImpl tmpItem = new ItemImpl(name, price);
        int pos = Collections.binarySearch(list, tmpItem, tmpItem);
        if (pos < 0) {
            return null;
        } else {
            return (Item) list.get(pos);
        }
    }

    @Override
    public synchronized boolean deleteItem(String name, int price) throws RemoteException {
        ArrayList<ItemImpl> list = this.items.get(name);
        if (list == null) {
            return false;
        }
        ItemImpl tmpItem = new ItemImpl(name, price);
        int pos = Collections.binarySearch(list, tmpItem, tmpItem);
        if (pos < 0) {
            return false;
        }
        boolean rv = (list.remove(pos) != null);
        if (list.size() == 0) {
            this.items.remove(name);
        }
        return rv;
    }

    @Override
    public Client addClient(String name) throws RemoteException {
        Client client;
        if ((client = this.clients.get(name)) != null) {
            System.err.println("User already exists");
        }  else {
            client = new ClientImpl(name);
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
}
