/** Immutable user: only the salted hash is kept, never the password. */
public final class User {
    private final String username, salt, passwordHash;

    public User(String username, String salt, String passwordHash) {
        this.username = username; this.salt = salt; this.passwordHash = passwordHash;
    }
    public String getUsername()     { return username; }
    public String getSalt()         { return salt; }
    public String getPasswordHash() { return passwordHash; }
}
