package is.mjuk.market.server;

import is.mjuk.market.common.Client;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import se.kth.id2212.ex2.bankrmi.Account;

public class ClientImpl extends UnicastRemoteObject implements Client {

    private String name;
    private Account bankAcc;

    private ClientImpl() throws RemoteException {
        // Do not allow default constructor
    }

    public ClientImpl(String name, Account bankAcc) throws RemoteException {
        this.name = name;
        this.bankAcc = bankAcc;
    }
    
    @Override
    public String getName() throws RemoteException {
        return this.name;
    }

    @Override
    public Account getAccount() throws RemoteException {
        return bankAcc;
    }

}

