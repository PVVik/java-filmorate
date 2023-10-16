package ru.yandex.practicum.filmorate.storage.db.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.storage.mapper.LikeMapper;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeDaoImpl implements LikeDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void like(Long filmId, Long userId) {
        log.debug("like({}, {})", filmId, userId);
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
        log.trace("The movie {} was liked by user {}", filmId, userId);
    }

    @Override
    public void dislike(Long filmId, Long userId) {
        log.debug("dislike({}, {})", filmId, userId);
        jdbcTemplate.update("DELETE FROM likes WHERE film_id=? AND user_id=?", filmId, userId);
        log.trace("The user {}, disliked the movie {}", userId, filmId);
    }

    @Override
    public int countLikes(Long filmId) {
        log.debug("countLikes({}).", filmId);
        Integer count = Objects.requireNonNull(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id=?", Integer.class, filmId));
        log.trace("The movie {} liked {} times", filmId, count);
        return count;
    }

    @Override
    public boolean isLiked(Long filmId, Long userId) {
        log.debug("isLiked({}, {})", filmId, userId);
        try {
            jdbcTemplate.queryForObject("SELECT film_id, user_id FROM likes WHERE film_id=? AND user_id=?",
                    new LikeMapper(), filmId, userId);
            log.trace("The movie {} was liked by user {}", filmId, userId);
            return true;
        } catch (EmptyResultDataAccessException exception) {
            log.trace("There is no like for film {} from user {}", filmId, userId);
            return false;
        }
    }
}
