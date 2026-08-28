package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new ConcurrentHashMap<>();

    @Override
    public User addUser(User user) {
        validNotNull(user);
        validEmail(user);
        if (user.getName() == null || user.getName().isBlank()) {
            addNameIfEmptyName(user);
        }
        user.setId(getId());
        user.setFriendsIds(new HashSet<>());
        users.put(user.getId(), user);
        log.info("Создали и добавили пользователя с id {}", user.getId());

        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!isExists(user)) {
            log.warn("Пользователь с переданным id {} не найден при обновлении", user.getId());
            throw new NotFoundException("Пользователя с переданным id не существует");
        }
        User oldUser = users.get(user.getId());

        if (StringUtils.hasText(user.getEmail())) {
            oldUser.setEmail(user.getEmail());
            log.info("Обновили email пользователя с id {}", oldUser.getId());
        }
        if (StringUtils.hasText(user.getLogin())) {
            oldUser.setLogin(user.getLogin());
            log.info("Обновили логин пользователя с id {}", oldUser.getId());
        }
        if (user.getBirthday() != null) {
            oldUser.setBirthday(user.getBirthday());
            log.info("Обновили дату рождения пользователя с id {}", oldUser.getId());
        }
        if (StringUtils.hasText(user.getName())) {
            oldUser.setName(user.getName());
            log.info("Обновили имя пользователя с id {}", oldUser.getId());
        }
        if (user.getFriendsIds() != null) {
            oldUser.setFriendsIds(user.getFriendsIds());
        }

        users.put(oldUser.getId(), oldUser);
        log.info("Завершили обновление пользователя с id {}", oldUser.getId());

        return oldUser;
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(long userId) {
        if (!users.containsKey(userId)) {
            log.warn("Запросили несуществующего пользователя по id {}", userId);
            throw new NotFoundException(String.format("Пользователь с id %d не найден", userId));
        }
        log.info("Получили пользователя с id {}", userId);
        return users.get(userId);
    }

    @Override
    public List<User> getFriends(long userId) {
        Set<Long> friendsIds = this.getUserById(userId).getFriendsIds();

        log.info("Запрошен и возвращен список друзей пользователя {}", userId);
        return this.getUsers().stream()
                .filter(user -> friendsIds.contains(user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(long userId, long friendId) {
        List<User> userFriends = this.getFriends(userId);

        log.info("Запрошен и возвращен список общих друзей пользователей {} и {}", userId, friendId);
        return this.getFriends(friendId).stream()
                .filter(userFriends::contains)
                .collect(Collectors.toList());
    }

    private long getId() {
        long maxId = users.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++maxId;
    }

    private void addNameIfEmptyName(User user) {
        user.setName(user.getLogin());
    }

    private void validNotNull(User user) {
        if (user.getEmail() == null) {
            log.warn("Не передан email при создании фильма");
            throw new ValidationException("email не может быть пустым");
        }
        if (user.getLogin() == null) {
            log.warn("Не передан логин при создании фильма");
            throw new ValidationException("login не может быть пустым");
        }
        if (user.getBirthday() == null) {
            log.warn("Не передана дата рождения при создании фильма");
            throw new ValidationException("Дата рождения не может быть пустой");
        }
    }

    private void validEmail(User user) {
        for (User addedUser : users.values()) {
            if (addedUser.getEmail().equals(user.getEmail())) {
                log.warn("Дублирование email: {}", user.getEmail());
                throw new ValidationException("Пользователь с таким email уже существует");
            }
        }
    }

    private boolean isExists(User user) {
        return users.containsKey(user.getId());
    }
}
