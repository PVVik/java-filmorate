package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Component
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("user_id");
        String email = rs.getString("email");
        String login = rs.getString("login");
        String name = rs.getString("user_name");
        LocalDate birthday = rs.getDate("birthday").toLocalDate();
        Long friendId = rs.getLong("friend_id");

        return User.builder().id(id).email(email).login(login).name(name).birthday(birthday)
                .friendsIds(new HashSet<>(List.of(friendId))).build();
    }
}
