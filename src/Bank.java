import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

/** All business rules live here. Main (the UI) never touches data directly. */
public class Bank {
    private static final int MAX_FAILS = 3;
    private static final long LOCK_MS = 5 * 60 * 1000;
    private static final String DUMMY_SALT = PasswordUtil.newSalt();
    private static final String DUMMY_HASH = PasswordUtil.hash("dummy".toCharArray(), DUMMY_SALT);
    private static final String LOGIN_ERROR = "Invalid username or password."; // same message every time

    private final DataStore store;
    private final Map<String, User> users;
    private final Map<String, Account> accounts;
    private final Map<String, Integer> fails = new HashMap<>();
    private final Map<String, Long> lockedUntil = new HashMap<>();
    private final SecureRandom rng = new SecureRandom();

    public Bank(DataStore store) throws IOException {
        this.store = store;
        this.users = store.loadUsers();
        this.accounts = store.loadAccounts();
    }

    public void register(String username, char[] password) throws BankException, IOException {
        Validator.username(username);
        Validator.password(password);
        if (users.containsKey(username)) throw new BankException("That username is taken.");
        String salt = PasswordUtil.newSalt();
        users.put(username, new User(username, salt, PasswordUtil.hash(password, salt)));
        store.saveUsers(users.values());
    }

    public User login(String username, char[] password) throws BankException {
        String key = username == null ? "" : username;
        if (lockedUntil.getOrDefault(key, 0L) > System.currentTimeMillis())
            throw new BankException("Account temporarily locked. Try again in 5 minutes.");
        User u = users.get(key);
        // Always do the slow hash, even for unknown users, so timing doesn't reveal who exists.
        boolean ok = false;
        if (u != null) ok = PasswordUtil.matches(password, u.getSalt(), u.getPasswordHash());
        else PasswordUtil.matches(password, DUMMY_SALT, DUMMY_HASH);
        if (ok) { fails.remove(key); return u; }
        int n = fails.merge(key, 1, Integer::sum);
        if (n >= MAX_FAILS) { lockedUntil.put(key, System.currentTimeMillis() + LOCK_MS); fails.remove(key); }
        throw new BankException(LOGIN_ERROR);
    }

    public Account openAccount(User user) throws IOException {
        String number;
        do { number = String.format("%010d", rng.nextLong(10_000_000_000L)); }
        while (accounts.containsKey(number));
        Account a = new Account(number, user.getUsername(), 0);
        accounts.put(number, a);
        store.saveAccounts(accounts.values());
        return a;
    }

    public List<Account> accountsOf(User user) {
        List<Account> mine = new ArrayList<>();
        for (Account a : accounts.values()) if (a.getOwner().equals(user.getUsername())) mine.add(a);
        return mine;
    }

    /** Authorization check: you can only touch accounts that belong to YOU. */
    public Account getOwned(User user, String number) throws BankException {
        Account a = accounts.get(number == null ? "" : number.trim());
        if (a == null || !a.getOwner().equals(user.getUsername())) throw new BankException("Account not found.");
        return a;
    }

    public Transaction deposit(User user, String number, long cents) throws BankException, IOException {
        Account a = getOwned(user, number);
        Transaction t = a.deposit(cents);
        persist(a, t);
        return t;
    }

    public Transaction withdraw(User user, String number, long cents) throws BankException, IOException {
        Account a = getOwned(user, number);
        Transaction t = a.withdraw(cents);
        persist(a, t);
        return t;
    }

    private void persist(Account a, Transaction t) throws IOException {
        store.saveAccounts(accounts.values());
        store.appendTransaction(t);
    }
}
