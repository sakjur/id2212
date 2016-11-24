package is.mjuk.market.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Market extends Remote {
    public int countItems() throws RemoteException;
    public String[] listItems() throws RemoteException;
    public Item addItem(String name, int price, Client owner) throws RemoteException;
    public Item getItem(String name, int price) throws RemoteException;
    public Item[] getItems(String name) throws RemoteException;
    public Item deleteItem(String name, int price) throws RemoteException;
    public Item buyItem(String name, int price, Client buyer) throws RemoteException;

    public Client addClient(String name) throws RemoteException;
    public Client getClient(String name) throws RemoteException;
    public Client deleteClient(Client client) throws RemoteException;
    public void registerObserver(Client client, MarketObserver observer)
        throws RemoteException;
    public boolean deleteObserver(Client client, MarketObserver observer)
        throws RemoteException;

    public void addSub(String item, int price, Client client) throws RemoteException; 
    public void deleteSub(String item, Client client) throws RemoteException; 
}
