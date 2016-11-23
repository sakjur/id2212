package is.mjuk.market.server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.net.MalformedURLException;

import is.mjuk.market.common.Market;

public class Marketplace {

    public Marketplace () {
        try {
            Market market = new MarketImpl();

            try {
                LocateRegistry.getRegistry(1099).list();
            } catch (RemoteException e) {
                LocateRegistry.createRegistry(1099);
            }

            Naming.rebind("market", market);
            System.out.println("[DONE] Started Java RMI Marketplace");
        } catch (RemoteException e) {
            System.exit(1);
        } catch (MalformedURLException e) {
            System.exit(2);
        }

    }

    public static void main(String[] argv) {
        System.out.println("[...] Starting Java RMI Marketplace");
        Marketplace market = new Marketplace();
    }

}
