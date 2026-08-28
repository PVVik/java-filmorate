package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public void addFriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.setFriend(friendId);
        friend.setFriend(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);

        log.info("Пользователь с id {} принял в друзья пользователя {}", userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.deleteFriend(friendId);
        friend.deleteFriend(userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);

        log.info("Пользователь с id {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> getFriends(long userId) {
        Set<Long> friendsIds = userStorage.getUserById(userId).getFriendsIds();

        log.info("Запрошен и возвращен список друзей пользователя {}", userId);
        return userStorage.getUsers().stream()
                .filter(user -> friendsIds.contains(user.getId()))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(long userId, long friendId) {
        List<User> userFriends = this.getFriends(userId);

        log.info("Запрошен и возвращен список общих друзей пользователей {} и {}", userId, friendId);
        return this.getFriends(friendId).stream()
                .filter(userFriends::contains)
                .collect(Collectors.toList());
    }
}
