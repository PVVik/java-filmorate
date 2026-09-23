package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(long userId, long friendId) {
        if (friendId == userId) {
            throw new ValidationException("Пользователь не может добавить с друзья сам себя");
        }
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
        userStorage.addFriend(userId, friendId);

        log.info("Пользователь с id {} послал заявку пользователю {}", userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        if (friendId == userId) {
            throw new ValidationException("Пользователь не может удалить сам себя из друзей");
        }

        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
        userStorage.deleteFriend(userId, friendId);

        log.info("Пользователь с id {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<UserDto> getFriends(long userId) {
        userStorage.getUserById(userId);

        log.info("Обработан запрос на получение списка друзей пользователя {}", userId);
        return userStorage.getFriends(userId).stream()
                .map(UserMapper::mapToDto)
                .toList();
    }

    public List<UserDto> getCommonFriends(long userId, long friendId) {
        log.info("Получены общие друзья пользователей с id {} и {}", userId, friendId);

        return userStorage.getCommonFriends(userId, friendId).stream()
                .map(UserMapper::mapToDto)
                .toList();
    }

    public UserDto addUser(UserDto userDto) {
        User user = userStorage.addUser(UserMapper.mapToUser(userDto));

        log.info("Создали пользователя с id {}", user.getId());
        return UserMapper.mapToDto(user);
    }

    public UserDto updateUser(UserDto userDto) {
        User savedUser = userStorage.getUserById(userDto.getId());
        User updatedUser = UserMapper.mapToUpdateUser(savedUser, userDto);

        log.info("Обновили данные пользователя с id {}", updatedUser.getId());
        return UserMapper.mapToDto(userStorage.updateUser(updatedUser));
    }

    public List<UserDto> getUsers() {
        log.info("Обработан запрос на получение всех пользователей");

        return userStorage.getUsers().stream()
                .map(UserMapper::mapToDto)
                .toList();
    }

    public UserDto getUserById(long userId) {
        log.info("Получили пользователя по id {}", userId);

        return UserMapper.mapToDto(userStorage.getUserById(userId));
    }
}
