package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class GenreDbStorage extends BaseRepository<Genre> implements GenreStorage {

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Genre getGenreById(long genreId) {
        String query = "SELECT * FROM genre WHERE genre_id = ?";

        log.debug("SELECT * FROM genre WHERE genre_id = {}", genreId);

        return findOne(query, genreId).orElseThrow(() ->
                new NotFoundException(String.format("Жанра с id %d не существует", genreId)));
    }

    @Override
    public List<GenreDto> getGenresByFilmId(long filmId) {
        String sql = """
                SELECT g.genre_id, g.genre_name
                FROM genre AS g
                JOIN film_genres AS fg ON fg.genre_id = g.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.genre_id ASC
                """;

        log.debug("SELECT g.genre_id, g.genre_name FROM genre AS g JOIN film_genres AS fg " +
                "ON fg.genre_id = g.genre_id WHERE fg.film_id = {}", filmId);

        return jdbc.query(sql, (rs, rowNum) -> {
            var dto = new GenreDto();
            dto.setId(rs.getLong("genre_id"));
            dto.setName(rs.getString("genre_name"));
            return dto;
        }, filmId);
    }

    @Override
    public List<Long> addFilmGenres(long filmId, List<Long> genres) {
        if (genres == null || genres.isEmpty()) {
            return List.of();
        }

        String query = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        log.debug("Вставляем связи film_id={} с {} жанрами", filmId, genres.size());

        genres.forEach(genreId -> jdbc.update(query, filmId, genreId));

        return new ArrayList<>(genres);
    }

    @Override
    public void removeFilmGenres(long filmId) {
        String query = "DELETE FROM film_genres WHERE film_id = ?";

        log.debug("DELETE FROM film_genres WHERE film_id = {}", filmId);

        delete(query, filmId);
    }

    public List<GenreDto> getGenresDto() {
        String sql = "SELECT genre_id, genre_name FROM genre";
        return jdbc.query(sql, (rs, rowNum) -> {
            var dto = new GenreDto();
            dto.setId(rs.getLong("genre_id"));
            dto.setName(rs.getString("genre_name"));
            return dto;
        });
    }

}
