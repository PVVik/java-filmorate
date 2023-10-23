package ru.yandex.practicum.filmorate.storage.db.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserMapper;

import java.sql.Date;
import java.util.List;

@Slf4j
@Component("UserDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User createUser(User user) {
        log.debug("createUser({})", user);
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) "
                        + "VALUES (?, ?, ?, ?)",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()));
        User thisUser = jdbcTemplate.queryForObject(
                "SELECT user_id, email, login, name, birthday "
                        + "FROM users "
                        + "WHERE email=?", new UserMapper(), user.getEmail());
        log.trace("{} user was added to the data base", thisUser);
        return thisUser;
    }

    @Override
    public User updateUser(User user) {
        log.debug("updateUser({})", user);
        jdbcTemplate.update("UPDATE users "
                        + "SET email=?, login=?, name=?, birthday=? "
                        + "WHERE user_id=?",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        User thisUser = getUserById(user.getId());
        log.trace("The user {} was updated in the data base", thisUser);
        return thisUser;
    }

    @Override
    public User getUserById(Long id) {
        log.debug("getUserById({})", id);
        User thisUser = jdbcTemplate.queryForObject(
                "SELECT user_id, email, login, name, birthday FROM users "
                        + "WHERE user_id=?", new UserMapper(), id);
        log.trace("The user {} was returned", thisUser);
        return thisUser;
    }

    @Override
    public List<User> getUsers() {
        log.debug("getUsers()");
        List<User> users = jdbcTemplate.query(
                "SELECT user_id, email, login, name, birthday FROM users ",
                new UserMapper());
        log.trace("These are users in the data base: {}", users);
        return users;
    }

    @Override
    public boolean isContains(Long id) {
        log.debug("isContains({})", id);
        try {
            getUserById(id);
            log.trace("The user with id {} was found", id);
            return true;
        } catch (EmptyResultDataAccessException exception) {
            log.trace("No information was found for user with id {}", id);
            return false;
        }
    }
}
