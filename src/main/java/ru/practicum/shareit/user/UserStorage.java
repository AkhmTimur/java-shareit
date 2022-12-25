package ru.practicum.shareit.user;


import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    public User addToUsers(User user);

    public User updateUser(User user);

    User getUserById(Integer userId);

    public void deleteUserById(Integer userId);

    public List<User> getAllUsers();
}
