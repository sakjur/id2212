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
                this.parser(line);
            }
        } catch (RemoteException e) {
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("IO Error");
            System.exit(3);
        }
    }

    public void parser(String line) throws RemoteException {
        String[] parts = line.split(" ");

        if (parts.length == 0) {
            return;
        }

        if (parts[0].equals("help")) {
            System.out.println("ID2212 TRADER BY EMILTU AT KTH");
            System.out.println("            MANUAL            ");
            System.out.println("help -- DISPLAY MANUAL");
            System.out.println("logout -- EXIT");
            System.out.println("list -- LIST ALL ITEMS");
            System.out.println("sell <item> <price> -- SELL AN ITEM");
            System.out.println("buy  <item> <price> -- BUY AN ITEM");
            System.out.println("find <item> -- LIST OCCURRENCES OF AN ITEM");
            System.out.println("unregister -- LOGOUT AND DELETE ACCOUNT");
        } else if (parts[0].equals("logout") || parts[0].equals("unregister")) {
            if (parts[0].equals("unregister")) {
                boolean deleted = marketplace.deleteClient(user);
                if (!deleted) {
                    System.err.println("Failed deleting account...");
                }
            }
            System.out.println("Logging out...");
            System.exit(0);
        } else if (parts[0].equals("list")) {
            String[] list = marketplace.listItems();
            if (list.length == 0) {
                System.out.println("No items listed. Maybe list an item?");
            }
            for (String item : list) {
                System.out.println(item);
            }
        } else if (parts[0].equals("sell") || parts[0].equals("buy")) {
            if (parts.length < 3) {
                System.out.format("Usage: %s <item> <price>\n", parts[0]);
                return;
            }
            String name = parts[1];
            int price = Integer.parseInt(parts[2]);
            if (parts[0].equals("sell")) {
                marketplace.addItem(name, price, user);
            } else {
                boolean bought = marketplace.deleteItem(name, price);
                if (bought) {
                    System.out.format("Bought a %s for %s\n", name, price);
                } else {
                    System.out.format("Could not buy a %s for %s\n", name, price);
                }
            }
        } else if (parts[0].equals("find")) {
            if (parts.length < 2) {
                System.out.println("Usage: find <item>");
                return;
            }
            Item[] items = marketplace.getItems(parts[1]);
            if (items == null) {
                System.out.format("No %s listed\n", parts[1]);
            }
            for (Item item : items) {
                System.out.format("%s %s\n", item.getName(), item.getPrice());
            }
        }
    }

}
