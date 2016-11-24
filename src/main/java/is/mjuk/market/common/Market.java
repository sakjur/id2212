package is.mjuk.market.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Market extends Remote {
    public int countItems() throws RemoteException;
    public String[] listItems() throws RemoteException;
    public Item addItem(String name, int price, Client owner) throws RemoteException;
    public Item getItem(String name, int price) throws RemoteException;
    public Item[] getItems(String name) throws RemoteException;
    public boolean deleteItem(String name, int price) throws RemoteException;

    public Client addClient(String name) throws RemoteException;
    public Client getClient(String name) throws RemoteException;
    public boolean deleteClient(Client client) throws RemoteException;
}