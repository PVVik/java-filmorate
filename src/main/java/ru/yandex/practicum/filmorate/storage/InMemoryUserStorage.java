package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private static Map<Integer, User> users;
    private int id;

    public InMemoryUserStorage() {
        users = new ConcurrentHashMap<>();
        id = 0;
    }

    @Override
    public User addUser(User user) {
        setId(user);
        users.put(user.getId(), user);
        log.info("Пользователь сохранён под id '{}'", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.info("Пользователь с id '{}' обновлён", user.getId());
            return user;
        } else {
            throw new ObjectNotFoundException("Пользователя с id " + user.getId() + " не существует.");
        }
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Общее количество пользователей: '{}'", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(int id) {
        if (!users.containsKey(id)) {
            throw new ObjectNotFoundException("Пользователя " + id + " не существует.");
        }
        return users.get(id);
    }

    @Override
    public void deleteUsers() {
        users.clear();
        log.info("Все пользователи удалены.");
    }

    private void setId(User user) {
        if (user.getId() <= 0) {
            user.setId(++id);
        }
    }
}
