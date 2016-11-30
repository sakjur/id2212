package is.mjuk.market.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Market extends Remote {
    public String[] listItems() throws RemoteException;
    public Item addItem(String session, String name, int price) throws RemoteException;
    public Item getItem(String name, int price) throws RemoteException;
    public Item[] getItems(String name) throws RemoteException;
    public Item deleteItem(String session, String name, int price) throws RemoteException;
    public Item buyItem(String session, String name, int price) throws RemoteException;

    public String login(String name, String password) throws RemoteException;
    public String register(String name, String password) throws RemoteException;
    public Client getClient(String session) throws RemoteException;
    public Client deleteClient(String session) throws RemoteException;
    public void registerObserver(String session, MarketObserver observer)
        throws RemoteException;
    public boolean deleteObserver(String session, MarketObserver observer)
        throws RemoteException;

    public void addSub(String session, String item, int price) throws RemoteException; 
    public void deleteSub(String session, String item) throws RemoteException; 
}
