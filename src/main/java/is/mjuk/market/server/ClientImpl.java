package is.mjuk.market.server;

import is.mjuk.market.common.Client;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientImpl extends UnicastRemoteObject implements Client {

    private String name;

    private ClientImpl() throws RemoteException {
        // Do not allow default constructor
    }

    public ClientImpl(String name) throws RemoteException {
        this.name = name;
    }
    
    @Override
    public String getName() throws RemoteException {
        return this.name;
    };

}

