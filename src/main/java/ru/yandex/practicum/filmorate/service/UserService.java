package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public void addFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if (user == null || friend == null) {
            throw new ObjectNotFoundException("Такого пользователя не существует");
        }
        user.addFriend(friendId);
        friend.addFriend(userId);
        log.info("Пользователь с id '{}' успешно добавлен в список друзей.", friend.getId());
    }

    public void deleteFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if (user == null || friend == null) {
            throw new ObjectNotFoundException("Такого пользователя не существует");
        }
        user.removeFriend(friendId);
        friend.removeFriend(userId);
        log.info("Пользователь с id '{}' успешно удалён из списка друзей.", friend.getId());
    }

    public List<User> getMutualFriends(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if (user == null || friend == null) {
            throw new ObjectNotFoundException("Такого пользователя не существует");
        }
        Set<Integer> userFriends = user.getFriends();
        Set<Integer> friendFriends = friend.getFriends();
        log.info("Выведены общие друзья пользователя '{}' и пользователя '{}'", user.getId(), friend.getId());
        if (userFriends.stream().anyMatch(friendFriends::contains)) {
            return userFriends.stream()
                    .filter(userFriends::contains)
                    .filter(friendFriends::contains)
                    .map(userStorage::getUserById).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public List<User> getFriends(int id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new ObjectNotFoundException("Такого пользователя не существует");
        }
        Set<Integer> friends = user.getFriends();
        if (friends.isEmpty()) {
            throw new ObjectNotFoundException("Список друзей пуст.");
        }
        log.info("Выведен список друзей.");
        return friends.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public UserStorage getUserStorage() {
        return userStorage;
    }
}

