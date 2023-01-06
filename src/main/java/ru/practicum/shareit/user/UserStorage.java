package ru.practicum.shareit.user;


import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    User addToUsers(User user);

    User updateUser(User user);

    User getUserById(Integer userId);

    void deleteUserById(Integer userId);

    List<User> getAllUsers();
}
