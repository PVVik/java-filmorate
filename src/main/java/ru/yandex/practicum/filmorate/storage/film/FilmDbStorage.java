package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.time.LocalDate;
import java.util.List;

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
        String query = "SELECT * FROM films";

        log.debug("SELECT * FROM films");

        return findMany(query);
    }

    @Override
    public Film getFilmById(long filmId) {
        String filmQuery = "SELECT film_id, film_name, description, release_date, " +
                "duration, mpa_id FROM films WHERE film_id = ?";

        log.debug("SELECT film_id, film_name, description, release_date, duration, mpa_id FROM films WHERE film_id = {}",
                filmId);

        return findOne(filmQuery, filmId).orElseThrow(() ->
                new NotFoundException(String.format("Фильма с id %d не существует", filmId)));
    }

    @Override
    public List<Film> getPopularFilms(long count) {
        String query = "SELECT films.*, COUNT(likes.film_id) AS count FROM films " +
                "LEFT JOIN likes ON likes.film_id = films.film_id " +
                "GROUP BY films.film_id ORDER BY count DESC LIMIT ?";

        log.debug("SELECT films.*, COUNT(likes.film_id) AS count FROM films " +
                "LEFT JOIN likes ON likes.film_id = films.film_id " +
                "GROUP BY films.film_id ORDER BY count DESC LIMIT {}", count);

        return jdbc.query(query, (rs, rowNum) -> {
            long id = rs.getLong("film_id");
            String name = rs.getString("film_name");
            String description = rs.getString("description");
            LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
            Long duration = rs.getLong("duration");
            long mpaId = rs.getLong("mpa_id");
            long count1 = rs.getLong("count");

            return Film.builder().id(id).name(name).description(description).releaseDate(releaseDate)
                    .duration(duration).mpaId(mpaId).build();
        }, count);
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


}
