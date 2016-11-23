package is.mjuk.market.client;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.NotBoundException;

import java.net.MalformedURLException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import is.mjuk.market.common.Client;
import is.mjuk.market.common.Item;
import is.mjuk.market.common.Market;

public class Trader {
    private Market marketplace;
    private Client user;
    private String name;

    public static void main(String[] argv) {
        Trader t = new Trader();
        t.run();
    }

    public Trader() {
        try {
            marketplace = (Market) Naming.lookup("market");
        } catch (RemoteException e) {
            System.err.println("Remote Exception");
        } catch (NotBoundException e) {
            System.err.println("Unbound Exception");
        } catch (MalformedURLException e) {
        }
    }

    public void run() {
        InputStreamReader stdin = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(stdin);
        String line;

        try {

            System.out.print("Username > ");
            line = in.readLine().split(" ")[0];
            user = marketplace.getClient(line);
            if (user == null) {
                user = marketplace.addClient(line);
            }

            System.out.format("Logged in as %s\n", user.getName());

            while (true) {
                System.out.format("%s > ", user.getName());
                line = in.readLine();
            /*marketplace.addItem("Counter", 2500, client);
            marketplace.addItem("Counter", 2500, client);
            for (String item : marketplace.listItems()) {
                System.out.println(item);
            }

            for (Item item : marketplace.getItems("Counter")) {
                System.out.format("%s %s\n", item.getName(), item.getPrice());
            }
            
            Item item = marketplace.getItem("Counter", 2500);
            if (item != null)
                System.out.format("%s %s\n", item.getName(), item.getPrice());
            

            if (marketplace.deleteItem("Counter", 2500)) {
                System.out.println("Deleted object");
            }

            for (String item : marketplace.listItems()) {
                System.out.println(item);
            }*/
            }
        } catch (RemoteException e) {
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("IO Error");
            System.exit(3);
        }
    }

    public void parser(String line) {
    }

}
