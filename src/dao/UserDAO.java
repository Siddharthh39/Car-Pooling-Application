package dao;

import db.DBConnection;
import model.User;

import java.sql.*;

public class UserDAO {

    // Register new user
    public int registerUser(User user) throws Exception {

        String sql = "INSERT INTO users(name, email) VALUES (?, ?)";

        Connection con = DBConnection.getConnection();
        PreparedStatement ps =
                con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, user.getName());
        ps.setString(2, user.getEmail());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }

    // Login check
    public boolean userExists(int userId) throws Exception {

        String sql = "SELECT id FROM users WHERE id=?";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userId);

        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    // Fetch user (optional)
    public User getUserById(int userId) throws Exception {

        String sql = "SELECT * FROM users WHERE id=?";
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email")
            );
        }
        return null;
    }
}
