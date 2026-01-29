package service;

import dao.UserDAO;
import model.User;

public class UserService {

    private UserDAO userDAO = new UserDAO();

    // login
    public boolean login(int userId) throws Exception {
        return userDAO.userExists(userId);
    }

    // register new user
    public int register(String name, String email) throws Exception {
        User user = new User(name, email);
        return userDAO.registerUser(user);
    }
}
