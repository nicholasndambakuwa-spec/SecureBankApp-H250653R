import java.io.Console;
import java.io.IOException;
import java.util.*;

/** Console menu (the "face" of the app). All rules are enforced in Bank, not here. */
public class Main {
    private static final long IDLE_LIMIT_MS = 5 * 60 * 1000;
    private static final Console console = System.console();
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            Bank bank = new Bank(new DataStore("data"));
            System.out.println("=== Secure Banking Application ===");
            while (true) {
                System.out.print("\n1) Login  2) Register  3) Exit\n> ");
                String c = line();
                if (c == null || c.equals("3")) break;
                try {
                    if (c.equals("1")) { User u = login(bank); if (u != null) session(bank, u); }
                    else if (c.equals("2")) register(bank);
                    else System.out.println("Please choose 1, 2 or 3.");
                } catch (BankException e) { System.out.println("Error: " + e.getMessage()); }
            }
            System.out.println("Goodbye!");
        } catch (IOException e) {
            System.err.println("Storage problem. Please check the data folder."); // no stack trace shown to users
        }
    }

    private static User login(Bank bank) throws BankException {
        System.out.print("Username: ");
        String name = line();
        char[] pw = password("Password: ");
        try { return bank.login(name, pw); } finally { Arrays.fill(pw, '\0'); }
    }

    private static void register(Bank bank) throws BankException, IOException {
        System.out.print("Choose username: ");
        String name = line();
        char[] pw = password("Choose password: ");
        try { bank.register(name, pw); System.out.println("Registered! You can log in now."); }
        finally { Arrays.fill(pw, '\0'); } // wipe password from memory
    }

    private static void session(Bank bank, User user) throws IOException {
        System.out.println("\nWelcome, " + user.getUsername() + "!");
        long last = System.currentTimeMillis();
        while (true) {
            System.out.print("\n1) New account  2) My accounts  3) Deposit  4) Withdraw  5) History  6) Logout\n> ");
            String c = line();
            if (c == null || c.equals("6")) { System.out.println("Logged out."); return; }
            if (System.currentTimeMillis() - last > IDLE_LIMIT_MS) { System.out.println("Session expired. Please log in again."); return; }
            last = System.currentTimeMillis();
            try {
                switch (c) {
                    case "1" -> System.out.println("Created account " + bank.openAccount(user).getNumber());
                    case "2" -> { for (Account a : bank.accountsOf(user)) System.out.println(a.getNumber() + "  balance: " + Money.format(a.getBalance())); }
                    case "3" -> move(bank, user, true);
                    case "4" -> move(bank, user, false);
                    case "5" -> bank.getOwned(user, ask("Account no: ")).getHistory().forEach(System.out::println);
                    default -> System.out.println("Please choose 1-6.");
                }
            } catch (BankException e) { System.out.println("Error: " + e.getMessage()); }
        }
    }

    private static void move(Bank bank, User user, boolean deposit) throws BankException, IOException {
        String acc = ask("Account no: ");
        long cents = Money.parse(ask("Amount: "));
        Transaction t = deposit ? bank.deposit(user, acc, cents) : bank.withdraw(user, acc, cents);
        System.out.println("Done. New balance: " + Money.format(t.getBalanceAfter()));
    }

    private static String ask(String prompt) { System.out.print(prompt); return line(); }

    private static String line() {
        String value = console != null ? console.readLine() : in.hasNextLine() ? in.nextLine() : null;
        return value == null ? null : value.trim();
    }

    /** Password input is visible in the console. */
    private static char[] password(String prompt) {
        if (console != null) {
            String value = console.readLine(prompt);
            return value == null ? new char[0] : value.toCharArray();
        }
        System.out.print(prompt);
        String s = line();
        return s == null ? new char[0] : s.toCharArray();
    }
}
