package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {
    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("review_id");
        String content = rs.getString("content");
        boolean isPositive = rs.getBoolean("is_positive");
        long userId = rs.getLong("user_id");
        long filmId = rs.getLong("film_id");
        long useful = rs.getLong("useful");

        return Review.builder().reviewId(id).content(content).isPositive(isPositive).userId(userId)
                .filmId(filmId).useful(useful).build();
    }
}
