package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User addUser(@RequestBody @Valid User user) {
        validNotNull(user);
        validEmail(user);
        if (user.getName() == null || user.getName().isBlank()) {
            addNameIfEmptyName(user);
        }
        user.setId(getId());
        users.put(user.getId(), user);
        log.info("Создали и добавили пользователя с id {}", user.getId());

        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User user) {
        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с переданным id {} не найден при обновлении", user.getId());
            throw new NoSuchElementException("Пользователя с переданным id не существует");
        }
        User oldUser = users.get(user.getId());

        if (user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
            log.info("Обновили email пользователя с id {}", oldUser.getId());
        }
        if (user.getLogin() != null) {
            oldUser.setLogin(user.getLogin());
            log.info("Обновили логин пользователя с id {}", oldUser.getId());
        }
        if (user.getBirthday() != null) {
            oldUser.setBirthday(user.getBirthday());
            log.info("Обновили дату рождения пользователя с id {}", oldUser.getId());
        }
        if (user.getName() != null) {
            oldUser.setName(user.getName());
            log.info("Обновили имя пользователя с id {}", oldUser.getId());
        }

        users.put(oldUser.getId(), oldUser);
        log.info("Завершили обновление пользователя с id {}", oldUser.getId());

        return oldUser;
    }

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
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
}
