package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
public class UserService {
    private UserStorage userStorage;

    public UserService(@Qualifier("InMemoryUserStorage")UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        return userStorage.addToUsers(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public User getUserById(Integer userId) {
        return userStorage.getUserById(userId);
    }

    public void deleteUserById(Integer userId) {
        userStorage.deleteUserById(userId);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }
}
