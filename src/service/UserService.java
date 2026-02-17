package service;

import dao.UserDAO;
import model.User;

import java.sql.SQLException;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public boolean login(int userId) throws SQLException {
        return userDAO.userExists(userId);
    }

    public int register(String name, String email) throws SQLException {
        User user = new User(name, email);
        return userDAO.registerUser(user);
    }

    public User getByEmail(String email) throws SQLException {
        return userDAO.getUserByEmail(email);
    }

    public User getById(int userId) throws SQLException {
        return userDAO.getUserById(userId);
    }
}
