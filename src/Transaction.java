import java.time.Instant;

/** Immutable record of one deposit or withdrawal. */
public final class Transaction {
    public enum Type { DEPOSIT, WITHDRAW }

    private final Instant time;
    private final String accountNumber;
    private final Type type;
    private final long amount, balanceAfter;

    public Transaction(Instant time, String accountNumber, Type type, long amount, long balanceAfter) {
        this.time = time; this.accountNumber = accountNumber; this.type = type;
        this.amount = amount; this.balanceAfter = balanceAfter;
    }

    public String getAccountNumber() { return accountNumber; }
    public long getBalanceAfter()    { return balanceAfter; }

    public String toLine() {
        return String.join("|", time.toString(), accountNumber, type.name(), String.valueOf(amount), String.valueOf(balanceAfter));
    }

    public static Transaction fromLine(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 5) throw new IllegalArgumentException("bad transaction line");
        return new Transaction(Instant.parse(p[0]), p[1], Type.valueOf(p[2]), Long.parseLong(p[3]), Long.parseLong(p[4]));
    }

    @Override public String toString() {
        return String.format("%s  %-8s %12s  balance %12s", time.toString().substring(0, 19).replace('T', ' '),
                type, Money.format(amount), Money.format(balanceAfter));
    }
}
