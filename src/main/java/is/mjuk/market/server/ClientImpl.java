package is.mjuk.market.server;

import is.mjuk.market.common.Client;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import se.kth.id2212.ex2.bankrmi.Account;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ClientImpl extends UnicastRemoteObject implements Client {

    private String name;
    private Integer id = null;
    private Account bankAcc;

    private ClientImpl() throws RemoteException {
        // Do not allow default constructor
    }

    public ClientImpl(String name, Account bankAcc) throws RemoteException {
        this.name = name;
        this.bankAcc = bankAcc;
    }

    public boolean store(Connection conn) {
        String insert = "INSERT INTO users "
            + "(username, passwd, active) VALUES "
            + "(?, ?, true)";
        try {
            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setString(1, this.name);
            stmt.setString(2, "password");
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
        return true;
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

