package ru.yandex.practicum.filmorate.storage.db.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreDaoImpl implements GenreDao {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre getGenreById(Integer id) {
        log.debug("getGenreById({})", id);
        Genre genre = jdbcTemplate.queryForObject("SELECT genre_id, genre_type FROM genre WHERE genre_id=?",
                new GenreMapper(), id);
        log.trace("The genre type with id {} was returned", id);
        return genre;
    }

    @Override
    public List<Genre> getGenres() {
        log.debug("getGenres()");
        List<Genre> genreList = jdbcTemplate.query("SELECT genre_id, genre_type FROM genre ORDER BY genre_id",
                new GenreMapper());
        log.trace("These are all genre types: {}", genreList);
        return genreList;
    }

    @Override
    public boolean isContains(Integer id) {
        log.debug("isContains({})", id);
        try {
            getGenreById(id);
            log.trace("The genre with id {} was found", id);
            return true;
        } catch (EmptyResultDataAccessException exception) {
            log.trace("No information for id {} was found", id);
            return false;
        }
    }
}
