import java.util.regex.Pattern;

/** Money is stored as whole cents (long) - never double - so there are no rounding errors. */
public final class Money {
    public static final long MAX_TRANSACTION = 1_000_000_00L; // 1,000,000.00
    private static final Pattern AMOUNT = Pattern.compile("\\d{1,7}(\\.\\d{1,2})?");

    private Money() {}

    public static long parse(String input) throws BankException {
        String s = input == null ? "" : input.trim();
        if (!AMOUNT.matcher(s).matches()) throw new BankException("Enter a valid amount, e.g. 150 or 150.75");
        String[] p = s.split("\\.");
        long cents = Long.parseLong(p[0]) * 100 + (p.length > 1 ? Long.parseLong((p[1] + "0").substring(0, 2)) : 0);
        if (cents <= 0) throw new BankException("Amount must be greater than zero.");
        if (cents > MAX_TRANSACTION) throw new BankException("Maximum per transaction is 1,000,000.00");
        return cents;
    }

    public static String format(long cents) {
        return String.format("%d.%02d", cents / 100, cents % 100);
    }
}
