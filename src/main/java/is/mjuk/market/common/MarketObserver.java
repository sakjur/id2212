package is.mjuk.market.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MarketObserver extends Remote {
    public void notifySub(Item item) throws RemoteException;
    public void notifySold(Item item) throws RemoteException;
    public long getId() throws RemoteException;
}

