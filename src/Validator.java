import java.util.regex.Pattern;

/** Allow-list input validation: accept ONLY what we expect, reject everything else. */
public final class Validator {
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{3,20}");
    private Validator() {}

    public static void username(String u) throws BankException {
        if (u == null || !USERNAME.matcher(u).matches())
            throw new BankException("Username: 3-20 letters, digits or underscore only.");
    }

    public static void password(char[] p) throws BankException {
        String s = new String(p);
        boolean ok = s.length() >= 8 && s.length() <= 64
                && s.chars().anyMatch(Character::isUpperCase)
                && s.chars().anyMatch(Character::isLowerCase)
                && s.chars().anyMatch(Character::isDigit)
                && s.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        if (!ok) throw new BankException("Password: 8-64 chars with upper, lower, digit and a symbol.");
    }
}
