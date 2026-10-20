package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DirectorRowMapper implements RowMapper<Director> {
    @Override
    public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("director_id");
        String name = rs.getString("director_name");

        return Director.builder().id(id).name(name).build();
    }
}
