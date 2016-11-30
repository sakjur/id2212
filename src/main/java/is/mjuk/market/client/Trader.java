package is.mjuk.market.client;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.NotBoundException;
import java.rmi.server.UnicastRemoteObject;

import java.net.MalformedURLException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.concurrent.ThreadLocalRandom;

import is.mjuk.market.common.Client;
import is.mjuk.market.common.Item;
import is.mjuk.market.common.Market;
import is.mjuk.market.common.MarketObserver;

import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.Account;
import se.kth.id2212.ex2.bankrmi.RejectedException;

public class Trader {
    private Market marketplace;
    private Client user;
    private String name;
    private MarketObserver observer;
    private Bank bank;
    private Account bankAcc;
    private String session;

    public static void main(String[] argv) {
        Trader t = new Trader();
        t.run();
    }

    public Trader() {
        try {
            marketplace = (Market) Naming.lookup("market");
            bank = (Bank) Naming.lookup("marketbank");
            observer = new MarketObserverImpl();
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
            System.out.print("login or register?");
            line = in.readLine();
            boolean register = false;
            if (line.charAt(0) == 'r') {
                register = true;
                System.out.println("Register new user");
            } else {
                System.out.println("Login");
            }

            System.out.print("Username > ");
            line = in.readLine().split(" ")[0];
            String username = line;
            System.out.print("Password > ");
            line = in.readLine().split(" ")[0];
            String password = line;
            if (!register) {
                session = marketplace.login(username, password);
            } else {
                session = marketplace.register(username, password);
            }

            if (session == null) {
                System.out.println("Could not login");
                System.exit(0);
            }

            user = marketplace.getClient(session);
            if (user == null) {
                System.out.println("Could not login");
                System.exit(0);
            }

            bankAcc = bank.getAccount(user.getName());
            if (bankAcc == null) {
                try {
                    bankAcc = bank.newAccount(user.getName());
                } catch (RejectedException e) {
                    System.err.println("Could not create new bank account");
                    System.exit(-3);
                }
            }
            
            marketplace.registerObserver(this.session, this.observer);
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
            System.out.println("sub <item> <price> -- SUBSCRIBE TO AN ITEM");
            System.out.println("find <item> -- LIST OCCURRENCES OF AN ITEM");
            System.out.println("unregister -- LOGOUT AND DELETE ACCOUNT");
            System.out.println("balance -- GET BANK BALANCE");
        } else if (parts[0].equals("logout") || parts[0].equals("unregister")) {
            if (parts[0].equals("unregister")) {
                Client deleted = marketplace.deleteClient(session);
                if (deleted == null) {
                    System.err.println("Failed deleting account...");
                }
            }
            marketplace.deleteObserver(session, observer);
            marketplace.deleteSession(session);
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
        } else if (parts[0].equals("sell") || parts[0].equals("buy")
                || parts[0].equals("sub")) {
            if (parts.length < 3) {
                System.out.format("Usage: %s <item> <price>\n", parts[0]);
                return;
            }
            String name = parts[1];
            int price = Integer.parseInt(parts[2]);
            if (parts[0].equals("sell")) {
                marketplace.addItem(session, name, price);
            } else if (parts[0].equals("buy")) {
                Item bought = marketplace.buyItem(this.session, name, price);
                if (bought != null) {
                    System.out.format("Bought a %s for %s\n", name, price);
                } else {
                    System.out.format("Could not buy a %s for %s\n", name, price);
                }
            } else {
                marketplace.addSub(this.session, name, price);
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
        } else if (parts[0].equals("balance")) {
            System.out.format("Your balance is %.2f\n", bankAcc.getBalance());
        }
    }

    private static class MarketObserverImpl extends UnicastRemoteObject
            implements MarketObserver {

        private long id;

        public MarketObserverImpl() throws RemoteException {
            this.id = ThreadLocalRandom.current().nextLong();
        }

        @Override
        public void notifySold(Item item) {
            try {
                System.out.format("\n¤ Sold %s for %s\n", item.getName(), item.getPrice());
            } catch(RemoteException e) {
                // Wat?
            }
        }

        @Override
        public void notifySub(Item item) {
            try {
                System.out.format("\n¤ %s now available for %s\n",
                    item.getName(),
                    item.getPrice());
            } catch(RemoteException e) {
                // Wat?
            }
        }

        @Override
        public long getId() {
            return this.id;
        }
    }
}
