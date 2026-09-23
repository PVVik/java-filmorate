package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;

@Qualifier("UserDbStorage")
@Repository
@Slf4j
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User addUser(User user) {
        String query = "INSERT INTO users (email, login, user_name, birthday) " +
                "VALUES(?, ?, ?, ?)";

        log.debug("INSERT INTO users (email, login, user_name, birthday) " +
                "VALUES({}, {}, {}, {})", user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());

        long id = insert(query, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        user.setId(id);
        return user;
    }

    @Override
    public User updateUser(User user) {
        String query = "UPDATE users SET email = ?, login = ?, user_name = ?, birthday = ? WHERE user_id = ?";

        log.debug("UPDATE users SET email = {}, login = {}, user_name = {}, birthday = {} WHERE user_id = {}",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

        update(
                query,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public List<User> getUsers() {
        String query = "SELECT * FROM users " +
                "LEFT JOIN friendship ON users.user_id = friendship.user_id";

        log.debug("SELECT * FROM users LEFT JOIN friendship ON users.user_id = friendship.user_id");

        return findMany(query);
    }

    @Override
    public User getUserById(long userId) {
        String query = "SELECT users.*, friendship.friend_id FROM users " +
                "LEFT JOIN friendship ON users.user_id = friendship.user_id " +
                "WHERE users.user_id = ?";

        log.debug("SELECT users.*, friendship.friend_id FROM users " +
                "LEFT JOIN friendship ON users.user_id = friendship.user_id " +
                "WHERE users.user_id = {}", userId);

        return findOne(query, userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователя с id %d не существует", userId)));
    }

    @Override
    public List<User> getFriends(long userId) {
        String query = "SELECT users.*, friendship.friend_id FROM users " +
                "LEFT JOIN friendship ON users.user_id = friendship.friend_id " +
                "WHERE friendship.user_id = ?";

        log.debug("SELECT users.*, friendship.friend_id FROM users " +
                "LEFT JOIN friendship ON users.user_id = friendship.friend_id " +
                "WHERE friendship.user_id = {}", userId);

        return findMany(query, userId);
    }

    @Override
    public List<User> getCommonFriends(long userId, long friendId) {
        String query = "SELECT u.*, f2.friend_id FROM users AS u " +
                "JOIN friendship AS f1 ON u.user_id = f1.friend_id " +
                "JOIN friendship AS f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? " +
                "AND f2.user_id = ?";

        log.debug("SELECT u.*, f2.friend_id FROM users AS u " +
                "JOIN friendship AS f1 ON u.user_id = f1.friend_id " +
                "JOIN friendship AS f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = {} AND f2.user_id = {}", userId, friendId);

        return findMany(query, userId, friendId);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        String query = "INSERT INTO friendship (user_id, friend_id, is_confirmed) " +
                "VALUES (?, ?, false)";

        log.debug("INSERT INTO friendship (user_id, friend_id, is_confirmed) " +
                "VALUES ({}, {}, false)", userId, friendId);

        insert(query, userId, friendId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        String query = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

        log.debug("DELETE FROM friendship WHERE user_id = {} AND friend_id = {}", userId, friendId);

        delete(query, userId, friendId);
    }

}
