package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class UserController {

    private Map<Integer, User> users = new HashMap<>();
    private int id = 0;

    @PostMapping(value = "/users")
    @ResponseBody
    public User addUser(@RequestBody User user) throws ValidationException {
        userValidate(user);
        users.put(user.getId(), user);
        log.info("Пользователь сохранён под id '{}'", user.getId());
        return user;
    }

    @PutMapping("/users")
    @ResponseBody
    public User updateUser(@RequestBody User user) throws ValidationException {
        userValidate(user);
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.info("Пользователь с id '{}' обновлён", user.getId());
        } else {
            throw new ValidationException("Пользователя с id " + user.getId() + " не существует.");
        }
        return user;
    }

    @GetMapping("/users")
    @ResponseBody
    public List<User> getAllUsers() {
        log.info("Общее количество пользователей: '{}'", users.size());
        return new ArrayList<>(users.values());
    }

    private void userValidate(User user) throws ValidationException {
        if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Введён неверный email.");
        }
        if (user.getLogin().isBlank()) {
            throw new ValidationException("Введён неверный логин.");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Введена неверная дата.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getId() <= 0) {
            user.setId(++id);
            log.info("Incorrect user identifier was set as '{}'", user.getId());
        }
    }
}


