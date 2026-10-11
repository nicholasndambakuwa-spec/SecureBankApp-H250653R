import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Passwords are NEVER stored. We store salt + slow hash (PBKDF2). */
public final class PasswordUtil {
    private static final int ITERATIONS = 210_000, KEY_BITS = 256;
    private static final SecureRandom RNG = new SecureRandom();
    private PasswordUtil() {}

    public static String newSalt() {
        byte[] s = new byte[16];
        RNG.nextBytes(s);
        return Base64.getEncoder().encodeToString(s);
    }

    public static String hash(char[] password, String saltB64) {
        PBEKeySpec spec = new PBEKeySpec(password, Base64.getDecoder().decode(saltB64), ITERATIONS, KEY_BITS);
        try {
            byte[] h = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(h);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Hashing unavailable");
        } finally {
            spec.clearPassword();
        }
    }

    /** Constant-time compare so attackers can't learn anything from timing. */
    public static boolean matches(char[] password, String saltB64, String expectedHash) {
        byte[] a = hash(password, saltB64).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}
