package is.mjuk.market.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Item extends Remote {
    public String getName() throws RemoteException;
    public int getPrice() throws RemoteException;
}
