package is.mjuk.market.server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;

import is.mjuk.market.common.Market;
import se.kth.id2212.ex2.bankrmi.Bank;

public class Marketplace {
    private Bank bank;

    public Marketplace () {
        try {
            bank = (Bank) Naming.lookup("marketbank");

            Market market = new MarketImpl(bank);

            try {
                LocateRegistry.getRegistry(1099).list();
            } catch (RemoteException e) {
                LocateRegistry.createRegistry(1099);
            }

            Naming.rebind("market", market);
            System.out.println("[DONE] Started Java RMI Marketplace");
        } catch (RemoteException e) {
            System.out.println(e);
            System.exit(1);
        } catch (MalformedURLException e) {
            System.out.println(e);
            System.exit(2);
        } catch (NotBoundException e) {
            System.err.println("Unbound Exception");
        }

    }

    public static void main(String[] argv) {
        System.out.println("[...] Starting Java RMI Marketplace");
        Marketplace market = new Marketplace();
    }

}
