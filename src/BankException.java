/** Safe, user-friendly error. Messages here are shown to the user, so they never leak internals. */
public class BankException extends Exception {
    private static final long serialVersionUID = 1L;
    public BankException(String message) { super(message); }
}
