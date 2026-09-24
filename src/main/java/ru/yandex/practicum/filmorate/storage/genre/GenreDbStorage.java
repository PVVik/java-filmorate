package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Slf4j
public class GenreDbStorage extends BaseRepository<Genre> implements GenreStorage {

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Genre> getGenres() {
        String query = "SELECT * FROM genre";

        log.debug("SELECT * FROM genre");

        return findMany(query);
    }

    @Override
    public Genre getGenreById(long genreId) {
        String query = "SELECT * FROM genre WHERE genre_id = ?";

        log.debug("SELECT * FROM genre WHERE genre_id = {}", genreId);

        return findOne(query, genreId).orElseThrow(() ->
                new NotFoundException(String.format("Жанра с id %d не существует", genreId)));
    }

    @Override
    public Set<Long> addFilmGenres(long filmId, Set<Long> genres) {
        String query = "INSERT INTO film_genres (film_id, genre_id) " +
                "VALUES (?, ?)";

        log.debug("INSERT INTO film_genres (film_id, genre_id) VALUES ({}, {Set<genres>})", filmId);

        genres.forEach(genreId -> jdbc.update(query, filmId, genreId));

        return new HashSet<>(genres);
    }

}
