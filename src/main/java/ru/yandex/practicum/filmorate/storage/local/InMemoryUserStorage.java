package ru.yandex.practicum.filmorate.storage.local;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users;
    private final Map<Long, Long> friends;
    private Long id;

    public InMemoryUserStorage() {
        users = new HashMap<>();
        friends = new HashMap<Long, Long>();
        id = 0L;
    }

    @Override
    public User createUser(User user) {
        validate(user);
        users.put(user.getId(), user);
        log.info("The user '{}' has been saved with the identifier '{}'", user.getEmail(), user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            validate(user);
            users.put(user.getId(), user);
            log.info("'{}' info with identifier '{}' was updated", user.getLogin(), user.getId());
            return user;
        } else {
            throw new ObjectNotFoundException("Attempt to update non-existing user");
        }
    }

    @Override
    public User getUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new ObjectNotFoundException("Attempt to reach non-existing user with id '" + id + "'");
        }
        return users.get(id);
    }

    @Override
    public List<User> getUsers() {
        log.info("The number of users: '{}'", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (friends.containsKey(userId)) {
            friends.put(userId, friendId);
        } else {
            throw new ObjectNotFoundException("Attempt to add to a friends list non-existing user " + userId);
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        if (friends.containsKey(userId)) {
            friends.remove(userId, friendId);
        } else {
            throw new ObjectNotFoundException("Attempt to remove from friends list non-existing user " + userId);
        }
    }

    @Override
    public List<User> getFriends(Long userId) {
        List<User> friendsList = new ArrayList<>();
        if (friends.containsKey(userId)) {
            for (Map.Entry<Long, Long> entry : friends.entrySet()) {
                Long id = entry.getKey();
                if (id.equals(userId)) {
                    User friend = getUserById(entry.getValue());
                    friendsList.add(friend);
                }
            }
        }
        return friendsList;
    }

    @Override
    public boolean isContains(Long id) {
        return false;
    }

    private void validate(User user) {
        if (user.getBirthday().isAfter(LocalDate.now()) || user.getBirthday() == null) {
            throw new ValidationException("Incorrect user's birthday with identifier '" + user.getId() + "'");
        }
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Incorrect user's email with identifier '" + user.getId() + "'");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("User's name with identifier '{}' was set as '{}'", user.getId(), user.getName());
        }
        if (user.getLogin().isBlank() || user.getLogin().isEmpty()) {
            throw new ValidationException("Incorrect user's login with identifier '" + user.getId() + "'");
        }
        if (user.getId() == null || user.getId() <= 0) {
            user.setId(++id);
            log.info("'{}' identifier was set as '{}'", user.getEmail(), user.getId());
        }
    }
}