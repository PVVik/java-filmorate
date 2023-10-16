package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.friendship.FriendshipDao;
import ru.yandex.practicum.filmorate.storage.db.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.db.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@Service
public class UserDbService {
    private final UserStorage userStorage;
    private final FriendshipDao friendshipDao;

    @Autowired
    public UserDbService(@Qualifier("UserDbStorage") UserDbStorage userStorage,
                         FriendshipDao friendshipDao) {
        this.userStorage = userStorage;
        this.friendshipDao = friendshipDao;
    }

    public User createUser(User user) {
        if (user.getId() != null) {
            if (userStorage.isContains(user.getId())) {
                throw new ObjectAlreadyExistsException(format("User with id %d already exists", user.getId()));
            } else {
                throw new IllegalArgumentException("Unable to set id manually");
            }
        }
        validate(user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        if (!userStorage.isContains(user.getId())) {
            throw new ObjectNotFoundException("Attempt to update non existing user");
        }
        validate(user);
        return userStorage.updateUser(user);
    }

    public User getUserById(Long id) {
        if (!userStorage.isContains(id)) {
            throw new ObjectNotFoundException(format("User with id %d wasn't found", id));
        }
        return userStorage.getUserById(id);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public void addFriend(Long userId, Long friendId) {
        checkIfFriend(userId, friendId);
        boolean isFriend = friendshipDao.isFriend(userId, friendId);
        friendshipDao.addFriend(userId, friendId, isFriend);
    }

    public void deleteFriend(Long userId, Long friendId) {
        cheIfNotFriend(userId, friendId);
        friendshipDao.deleteFriend(userId, friendId);
    }

    public List<User> getFriendsList(Long id) {
        if (!userStorage.isContains(id)) {
            throw new ObjectNotFoundException(format("User with id %d wasn't found", id));
        }
        List<User> friends = friendshipDao.getFriends(id).stream()
                .mapToLong(Long::valueOf)
                .mapToObj(userStorage::getUserById)
                .collect(Collectors.toList());
        log.trace("The user's friends list were returned: {}", friends);
        return friends;
    }

    public List<User> getCommonFriends(Long userId, Long friendId) {
        if (!userStorage.isContains(userId)) {
            throw new ObjectNotFoundException(format("The user with id %d wasn't found", userId));
        }
        if (!userStorage.isContains(friendId)) {
            throw new ObjectNotFoundException(format("The user with id %d wasn't found", friendId));
        }
        if (userId.equals(friendId)) {
            throw new ObjectAlreadyExistsException("Unable to fet common friends list, id is " + userId);
        }
        List<User> userFriends = getFriendsList(userId);
        List<User> friendFriends = getFriendsList(friendId);
        return friendFriends.stream()
                .filter(userFriends::contains)
                .filter(friendFriends::contains)
                .collect(Collectors.toList());
    }

    private void validate(User user) {
        log.debug("validate({})", user);
        if (user.getBirthday().isAfter(LocalDate.now()) || user.getBirthday() == null) {
            throw new ValidationException(format("Incorrect user's birthday with identifier %d", user.getId()));
        }
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException(format("Incorrect user's email with identifier %d", user.getId()));
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("User's name with identifier {} was set as {}", user.getId(), user.getName());
        }
        if (user.getLogin().isBlank() || user.getLogin().isEmpty()) {
            throw new ValidationException(format("Incorrect user's login with identifier %d", user.getId()));
        }
    }

    private void checkIfFriend(Long userId, Long friendId) {
        log.debug("checkIfFriend({}, {})", userId, friendId);
        if (!userStorage.isContains(userId)) {
            throw new ObjectNotFoundException(format("User with id %d wasn't found", userId));
        }
        if (!userStorage.isContains(friendId)) {
            throw new ObjectNotFoundException(format("User with id %d wasn't found", userId));
        }
        if (userId.equals(friendId)) {
            throw new ObjectAlreadyExistsException("Attempt to add yourself into a friends list, the id is " + userId);
        }
        if (friendshipDao.isFriend(userId, friendId)) {
            throw new ValidationException(
                    format("The user with id %d is already friend of user with id %d", userId, friendId));
        }
    }

    private void cheIfNotFriend(Long userId, Long friendId) {
        log.debug("checkIfNotFriend({}, {})", userId, friendId);
        if (!userStorage.isContains(userId)) {
            throw new ObjectNotFoundException(format("User with id %d wasn't found", userId));
        }
        if (!userStorage.isContains(friendId)) {
            throw new ObjectNotFoundException(format("User with id %d wasn't found", userId));
        }
        if (userId.equals(friendId)) {
            throw new ObjectAlreadyExistsException(
                    "Attempt to delete yourself from a friends list, the id is " + userId);
        }
        if (!friendshipDao.isFriend(userId, friendId)) {
            throw new ValidationException(
                    format("There is no friendship between user with id %d and user with id %d", userId, friendId));
        }
    }
}