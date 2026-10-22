package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;
    private final EventService eventService;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage, EventService eventService) {
        this.userStorage = userStorage;
        this.eventService = eventService;
    }

    @Transactional
    public void addFriend(long userId, long friendId) {
        if (friendId == userId) {
            throw new ValidationException("Пользователь не может добавить с друзья сам себя");
        }
        var user = userStorage.getUserById(userId);
        var friend = userStorage.getUserById(friendId);
        userStorage.addFriend(user.getId(), friend.getId());
        eventService.addFriendEvent(userId, friendId, Operation.ADD);

        log.info("Пользователь с id {} послал заявку пользователю {}", userId, friendId);
    }

    @Transactional
    public void deleteFriend(long userId, long friendId) {
        if (friendId == userId) {
            throw new ValidationException("Пользователь не может удалить сам себя из друзей");
        }

        var user = userStorage.getUserById(userId);
        var friend = userStorage.getUserById(friendId);
        userStorage.deleteFriend(user.getId(), friend.getId());
        eventService.addFriendEvent(userId, friendId, Operation.REMOVE);

        log.info("Пользователь с id {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<UserDto> getFriends(long userId) {
        checkUserExists(userId);

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
        var user = userStorage.addUser(UserMapper.mapToUser(userDto));

        log.info("Создали пользователя с id {}", user.getId());
        return UserMapper.mapToDto(user);
    }

    public UserDto updateUser(UserDto userDto) {
        var savedUser = userStorage.getUserById(userDto.getId());
        var updatedUser = UserMapper.mapToUpdateUser(savedUser, userDto);

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

    public void deleteUser(long userId) {
        checkUserExists(userId);
        userStorage.deleteUser(userId);
        log.info("Удалили пользователя с id {}", userId);
    }

    public void checkUserExists(long userId) {
        getUserById(userId);
    }

}
