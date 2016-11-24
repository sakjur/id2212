package is.mjuk.market.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

import se.kth.id2212.ex2.bankrmi.Account;

public interface Client extends Remote {

    public String getName() throws RemoteException;
    public Account getAccount() throws RemoteException;

}

