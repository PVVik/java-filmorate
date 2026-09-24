package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.*;

@Qualifier("FilmDbStorage")
@Repository
@Slf4j
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Film addFilm(Film film) {
        String query = "INSERT INTO films (film_name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        log.debug("INSERT INTO films (film_name, description, release_date, duration, mpa_id) " +
                        "VALUES ({}, {}, {}, {}, {})", film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpaId());

        long id = insert(query, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpaId());
        film.setId(id);

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String query = "UPDATE films SET film_name = ?, description = ?, release_date = ?, " +
                "duration = ? WHERE film_id = ?";

        log.debug("UPDATE films SET film_name = {}, description = {}, release_date = {}, duration = {} WHERE film_id = {}",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getId());

        update(query, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getId());
        return film;
    }

    @Override
    public List<Film> getFilms() {
        String query = "SELECT f.film_id, f.film_name, f.description, f.release_date, " +
                "f.duration, f.mpa_id, fg.genre_id " +
                "FROM films AS f " +
                "LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id";

        log.debug("SELECT f.film_id, f.film_name, f.description, f.release_date, " +
                "f.duration, f.mpa_id, fg.genre_id " +
                "FROM films AS f " +
                "LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id");

        return jdbc.query(query, filmWithGenresExtractor);
    }

    @Override
    public Film getFilmById(long filmId) {
        String query = "SELECT f.film_id, f.film_name, f.description, f.release_date, " +
                "f.duration, f.mpa_id, fg.genre_id FROM films AS f " +
                "LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id " +
                "WHERE f.film_id = ?";

        log.debug("SELECT f.film_id, f.film_name, f.description, f.release_date, " +
                "f.duration, f.mpa_id, fg.genre_id FROM films AS f " +
                "LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id " +
                "WHERE f.film_id = {}", filmId);

        var film = jdbc.query(query, filmWithGenresExtractor, filmId);

        if (film == null || film.isEmpty()) {
            throw new NotFoundException(String.format("Фильм с id %d не был найден", filmId));
        } else return film.getFirst();
    }

    @Override
    public List<Film> getPopularFilms(long count) {
        String query = "SELECT f.film_id, f.film_name, f.description, f.release_date, " +
                "f.duration, f.mpa_id, fg.genre_id, " +
                "COUNT(DISTINCT l.user_id) AS like_count " +
                "FROM films AS f " +
                "LEFT JOIN likes AS l ON l.film_id = f.film_id " +
                "LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id " +
                "GROUP BY f.film_id, f.film_name, f.description, f.release_date, " +
                "f.duration, f.mpa_id, fg.genre_id " +
                "ORDER BY like_count DESC " +
                "LIMIT ?";

        log.debug("SELECT f.film_id, f.film_name, f.description, f.release_date, " +
                "f.duration, f.mpa_id, fg.genre_id, " +
                "COUNT(DISTINCT l.user_id) AS like_count " +
                "FROM films f " +
                "LEFT JOIN likes l ON l.film_id = f.film_id " +
                "LEFT JOIN film_genres fg ON fg.film_id = f.film_id " +
                "GROUP BY f.film_id, f.film_name, f.description, f.release_date, " +
                "f.duration, f.mpa_id, fg.genre_id " +
                "ORDER BY like_count DESC " +
                "LIMIT {}", count);

        return jdbc.query(query, filmWithGenresExtractor, count);
    }

    @Override
    public void addLike(long filmId, long userId) {
        String query = "INSERT INTO likes (film_id, user_id) " +
                "VALUES (?, ?)";

        log.debug("INSERT INTO likes (film_id, user_id) VALUES ({}, {})", filmId, userId);

        insert(query, filmId, userId);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

        log.debug("DELETE FROM likes WHERE film_id = {} AND user_id = {}", filmId, userId);

        delete(query, filmId, userId);
    }

    private final ResultSetExtractor<List<Film>> filmWithGenresExtractor = rs -> {
        Map<Long, Film> filmMap = new LinkedHashMap<>();
        Set<Long> genreIds = new LinkedHashSet<>();

        while (rs.next()) {
            long filmId = rs.getLong("film_id");
            Film film = filmMap.get(filmId);

            if (film == null) {
                film = Film.builder()
                        .id(filmId)
                        .name(rs.getString("film_name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getDate("release_date").toLocalDate())
                        .duration(rs.getLong("duration"))
                        .mpaId(rs.getLong("mpa_id"))
                        .genres(new LinkedHashSet<>())
                        .likes(new HashSet<>())
                        .build();
                filmMap.put(filmId, film);
            }

            long genreId = rs.getLong("genre_id");
            if (!rs.wasNull()) {
                film.getGenres().add(genreId);
            }
        }

        return new ArrayList<>(filmMap.values());
    };
}