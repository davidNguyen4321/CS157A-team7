package Auth;

import java.sql.*;
import java.time.ZoneId;
import java.util.regex.Pattern;
import Search.DatabaseAccess;

public class UserDAO {
    public static class User {
        public int id;
        public String name, imagePath, description, timezone, status, role;
    }

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9 _.\\-]{3,50}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String COLS =
        "USER_ID, USER_NAME, USER_IMAGE_PATH, USER_DESCRIPTION, TIMEZONE, USER_STATUS, ROLE";

    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.id = rs.getInt("USER_ID");
        u.name = rs.getString("USER_NAME");
        u.imagePath = rs.getString("USER_IMAGE_PATH");
        u.description = rs.getString("USER_DESCRIPTION");
        u.timezone = rs.getString("TIMEZONE");
        u.status = rs.getString("USER_STATUS");
        u.role = rs.getString("ROLE");
        return u;
    }

    /** Returns the user if credentials are valid and the account is Active, else null. */
    public static User authenticate(String login, String password) throws SQLException {
        String sql = "SELECT " + COLS + ", PASSWORD_HASH FROM USERS WHERE USER_NAME = ? OR EMAIL = ?";
        try (PreparedStatement ps = DatabaseAccess.getConnection().prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                if (!PasswordUtil.verify(password, rs.getString("PASSWORD_HASH"))) return null;
                User u = map(rs);
                return "Active".equals(u.status) ? u : null;
            }
        }
    }

    /** Returns null on success, otherwise an error message to show the user. */
    public static String create(String username, String email, String password, String timezone)
            throws SQLException {
        if (!USERNAME.matcher(username).matches())
            return "Username must be 3-50 characters (letters, numbers, spaces, _ . -).";
        if (email.length() > 254 || !EMAIL.matcher(email).matches())
            return "Please enter a valid email address.";
        if (password.length() < 8 || password.length() > 128)
            return "Password must be 8-128 characters.";
        if (timezone == null || !ZoneId.getAvailableZoneIds().contains(timezone)
                || timezone.length() > 30)
            timezone = "UTC";

        String sql = "INSERT INTO USERS (USER_NAME, ROLE, TIMEZONE, PASSWORD_HASH, EMAIL) "
                   + "VALUES (?, 'User', ?, ?, ?)";
        try (PreparedStatement ps = DatabaseAccess.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, timezone);
            ps.setString(3, PasswordUtil.hash(password));
            ps.setString(4, email);
            ps.executeUpdate();
            return null;
        } catch (SQLIntegrityConstraintViolationException dup) {
            String msg = String.valueOf(dup.getMessage()).toUpperCase();
            return msg.contains("EMAIL") ? "That email is already registered."
                                         : "That username is already taken.";
        }
    }

    public static User findById(int id) throws SQLException {
        try (PreparedStatement ps = DatabaseAccess.getConnection()
                .prepareStatement("SELECT " + COLS + " FROM USERS WHERE USER_ID = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }
}