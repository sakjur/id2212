package is.mjuk.market.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

import se.kth.id2212.ex2.bankrmi.Account;

public interface Session extends Remote {
    public String getSessionKey() throws RemoteException;
    public String getName() throws RemoteException;
}

