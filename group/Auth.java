import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Everything about users: registering, logging in, and remembering who is logged in.
 *
 * Passwords are NEVER saved as plain text. We save a salted PBKDF2 hash, so even
 * somebody who can read the users table cannot see the real passwords.
 */
public class Auth {

    private static final int ITERATIONS = 600_000;   // how many times the hash is repeated (slows down guessing)

    private static String currentUser = "";          // full name of the person who is logged in
    private static boolean tableReady = false;

    public static String getCurrentUser() { return currentUser; }

    public static void logout() { currentUser = ""; }

    // ------------------------------------------------------------------ database

    /** Creates the users table the first time (does nothing if it already exists). */
    public static void ensureUsersTable() throws SQLException {
        if (tableReady) return;
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "full_name VARCHAR(100) NOT NULL, "
                + "username VARCHAR(50) NOT NULL UNIQUE, "
                + "password_hash VARCHAR(255) NOT NULL, "
                + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)";
        try (Connection con = Main.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        }
        tableReady = true;
    }

    /** Saves a new user. Returns false if that username is already taken. */
    public static boolean register(String fullName, String username, char[] password) throws SQLException {
        ensureUsersTable();
        String sql = "INSERT INTO users (full_name, username, password_hash) VALUES (?, ?, ?)";
        try (Connection con = Main.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, username);
            ps.setString(3, hash(password));
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) return false;   // 1062 = duplicate entry -> username already exists
            throw ex;
        }
    }

    /** Checks the username and password. If they are right, remembers who logged in and returns true. */
    public static boolean login(String username, char[] password) throws SQLException {
        ensureUsersTable();
        String sql = "SELECT full_name, password_hash FROM users WHERE username = ?";
        try (Connection con = Main.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && verify(password, rs.getString("password_hash"))) {
                    currentUser = rs.getString("full_name");
                    return true;
                }
            }
        }
        return false;
    }

    /** Turns a technical SQL error into a short message a person can act on. */
    public static String explain(SQLException ex) {
        String msg = String.valueOf(ex.getMessage());
        if (msg.contains("No suitable driver") || msg.contains("ClassNotFound")) {
            return "The MySQL driver (.jar file) is not added to your project.";
        }
        if (ex.getErrorCode() == 1045) {
            return "MySQL refused the username/password that is written in Main.java (DB_USER / DB_PASS).";
        }
        if (ex.getErrorCode() == 1049) {
            return "The database was not found. Create the icf_inventory database first.";
        }
        if ("08S01".equals(ex.getSQLState()) || msg.contains("Communications link failure")) {
            return "Cannot reach MySQL. Is the MySQL server running?";
        }
        return "Database error: " + msg.split("\\R")[0];       // first line only
    }

    // ------------------------------------------------------------------ passwords

    /** Returns text like  pbkdf2$600000$<salt>$<hash>  (the salt is random for every user). */
    private static String hash(char[] password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, ITERATIONS, 256);
        return "pbkdf2$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /** Hashes the typed password the same way and compares it with the saved hash. */
    private static boolean verify(char[] password, String stored) {
        try {
            String[] parts = stored.split("\\$");
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password, salt, iterations, expected.length * 8);
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException ex) {
            return false;                                        // saved value is not in the expected format
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bits) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bits);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        } finally {
            spec.clearPassword();
        }
    }
}
