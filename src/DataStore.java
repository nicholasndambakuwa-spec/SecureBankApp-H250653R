import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

/** Text-file persistence: data/users.txt, data/accounts.txt, data/transactions.txt (pipe-separated). */
public class DataStore {
    private final Path dir, usersFile, accountsFile, txFile;

    public DataStore(String folder) throws IOException {
        dir = Paths.get(folder);
        Files.createDirectories(dir);
        usersFile = dir.resolve("users.txt");
        accountsFile = dir.resolve("accounts.txt");
        txFile = dir.resolve("transactions.txt");
        for (Path p : List.of(usersFile, accountsFile, txFile)) {
            if (!Files.exists(p)) Files.createFile(p);
            lockDown(p);
        }
    }

    public Map<String, User> loadUsers() throws IOException {
        Map<String, User> users = new LinkedHashMap<>();
        for (String line : lines(usersFile)) {
            try {
                String[] p = line.split("\\|", -1);
                if (p.length != 3) throw new IllegalArgumentException();
                Validator.username(p[0]);
                users.put(p[0], new User(p[0], p[1], p[2]));
            } catch (Exception e) { System.err.println("Skipped a corrupt line in users.txt"); }
        }
        return users;
    }

    public Map<String, Account> loadAccounts() throws IOException {
        Map<String, Account> accounts = new LinkedHashMap<>();
        for (String line : lines(accountsFile)) {
            try {
                String[] p = line.split("\\|", -1);
                if (p.length != 3) throw new IllegalArgumentException();
                long bal = Long.parseLong(p[2]);
                if (bal < 0) throw new IllegalArgumentException();
                accounts.put(p[0], new Account(p[0], p[1], bal));
            } catch (Exception e) { System.err.println("Skipped a corrupt line in accounts.txt"); }
        }
        for (String line : lines(txFile)) {
            try {
                Transaction t = Transaction.fromLine(line);
                Account a = accounts.get(t.getAccountNumber());
                if (a != null) a.addToHistory(t);
            } catch (Exception e) { System.err.println("Skipped a corrupt line in transactions.txt"); }
        }
        return accounts;
    }

    public void saveUsers(Collection<User> users) throws IOException {
        List<String> out = new ArrayList<>();
        for (User u : users) out.add(String.join("|", u.getUsername(), u.getSalt(), u.getPasswordHash()));
        writeAtomically(usersFile, out);
    }

    public void saveAccounts(Collection<Account> accounts) throws IOException {
        List<String> out = new ArrayList<>();
        for (Account a : accounts) out.add(String.join("|", a.getNumber(), a.getOwner(), String.valueOf(a.getBalance())));
        writeAtomically(accountsFile, out);
    }

    public void appendTransaction(Transaction t) throws IOException {
        Files.writeString(txFile, t.toLine() + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.APPEND);
    }

    /** Write to a temp file, then swap it in. A crash can never leave a half-written file. */
    private void writeAtomically(Path target, List<String> lines) throws IOException {
        Path tmp = Files.createTempFile(dir, "tmp", ".part");
        Files.write(tmp, lines, StandardCharsets.UTF_8);
        try { Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (AtomicMoveNotSupportedException e) { Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING); }
        lockDown(target);
    }

    private List<String> lines(Path p) throws IOException {
        List<String> out = new ArrayList<>();
        for (String l : Files.readAllLines(p, StandardCharsets.UTF_8)) if (!l.isBlank()) out.add(l.trim());
        return out;
    }

    /** Owner-only file permissions (works on Linux/Mac; silently skipped on Windows). */
    private void lockDown(Path p) {
        try { Files.setPosixFilePermissions(p, PosixFilePermissions.fromString("rw-------")); }
        catch (UnsupportedOperationException | IOException ignored) { }
    }
}
