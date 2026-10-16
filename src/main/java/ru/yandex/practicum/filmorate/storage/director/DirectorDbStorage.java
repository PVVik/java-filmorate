package ru.yandex.practicum.filmorate.storage.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Slf4j
public class DirectorDbStorage extends BaseRepository<Director> implements DirectorStorage {

    public DirectorDbStorage(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Director create(Director director) {
        String query = "INSERT INTO directors (director_name) VALUES (?)";

        log.debug("INSERT INTO directors (director_name) VALUES ({})", director.getName());

        long id = insert(query, director.getName());
        director.setId(id);

        return director;
    }

    @Override
    public Director update(Director director) {
        String query = "UPDATE directors SET director_name = ? WHERE director_id = ?";

        log.debug("UPDATE director SET director_name = {} WHERE director_id = {}", director.getName(), director.getId());

        update(query, director.getName(), director.getId());

        return director;
    }

    @Override
    public List<Director> getAll() {
        String query = "SELECT director_id, director_name FROM directors";

        log.debug("SELECT director_id, director_name FROM directors");

        return findMany(query);
    }

    @Override
    public Director getById(long id) {
        String query = "SELECT director_id, director_name FROM directors WHERE director_id = ?";

        log.debug("SELECT director_id, director_name FROM directors WHERE director_id = {}", id);

        return findOne(query, id).orElseGet(() -> {
            log.warn("Режиссер с id {} не найден", id);
            throw new NotFoundException(String.format("Режиссер с id %d не найден", id));
        });
    }

    @Override
    public void delete(long id) {
        String query = "DELETE FROM directors WHERE director_id = ?";

        log.debug("DELETE FROM directors WHERE director_id = {}", id);

        delete(query, id);
    }

    @Override
    public Set<Long> addFilmDirectors(long filmId, Set<Long> directors) {
        String query = "INSERT INTO film_directors (film_id, director_id) " +
                "VALUES (?, ?)";

        log.debug("INSERT INTO film_genres (film_id, director_id) VALUES ({}, {Set<directors>})", filmId);

        directors.forEach(director_id -> jdbc.update(query, filmId, director_id));

        return new HashSet<>(directors);
    }
}
