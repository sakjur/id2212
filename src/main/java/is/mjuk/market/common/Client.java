package is.mjuk.market.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {

    public String getName() throws RemoteException;

}

