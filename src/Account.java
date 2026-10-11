import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Encapsulation: balance is private and can ONLY change through deposit()/withdraw(). */
public class Account {
    private final String number, owner;
    private long balance; // cents
    private final List<Transaction> history = new ArrayList<>();

    public Account(String number, String owner, long balance) {
        this.number = number; this.owner = owner; this.balance = balance;
    }

    public String getNumber()  { return number; }
    public String getOwner()   { return owner; }
    public long getBalance()   { return balance; }
    public List<Transaction> getHistory() { return Collections.unmodifiableList(history); } // no outside edits
    public void addToHistory(Transaction t) { history.add(t); }

    public Transaction deposit(long cents) throws BankException {
        try { balance = Math.addExact(balance, cents); }
        catch (ArithmeticException e) { throw new BankException("Balance limit exceeded."); }
        return record(Transaction.Type.DEPOSIT, cents);
    }

    public Transaction withdraw(long cents) throws BankException {
        if (cents > balance) throw new BankException("Insufficient funds.");
        balance -= cents;
        return record(Transaction.Type.WITHDRAW, cents);
    }

    private Transaction record(Transaction.Type type, long cents) {
        Transaction t = new Transaction(Instant.now(), number, type, cents, balance);
        history.add(t);
        return t;
    }
}
