package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public void addFriend(long userId, long friendId) {
        if (friendId == userId) {
            throw new ValidationException("Пользователь не может добавить с друзья сам себя");
        }

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.setFriend(friendId);
        friend.setFriend(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);

        log.info("Пользователь с id {} принял в друзья пользователя {}", userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        if (friendId == userId) {
            throw new ValidationException("Пользователь не может удалить сам себя из друзей");
        }

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.deleteFriend(friendId);
        friend.deleteFriend(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);

        log.info("Пользователь с id {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> getFriends(long userId) {
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(long userId, long friendId) {
        return userStorage.getCommonFriends(userId, friendId);
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(long userId) {
        return userStorage.getUserById(userId);
    }
}
