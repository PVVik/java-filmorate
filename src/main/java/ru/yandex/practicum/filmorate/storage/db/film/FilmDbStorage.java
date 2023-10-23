package ru.yandex.practicum.filmorate.storage.db.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.db.mpa.MpaDao;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("FilmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaDao mpaDao;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<Film> getPopular(Integer count) {
        log.debug("getPopular()");

        List<Film> films = jdbcTemplate.query("SELECT * FROM films "
                + "LEFT JOIN likes ON likes.film_id = films.film_id "
                + "GROUP BY films.film_id "
                + "ORDER BY COUNT (likes.film_id) DESC "
                + "LIMIT ?", new FilmMapper(), count);

        for (Film film : films) {
            film.setMpa(mpaDao.getMpaById(film.getMpa().getId()));
            film.setGenres(getGenres(film.getId()));
        }

        return films;
    }

    @Override
    public Film createFilm(Film film) {
        log.debug("createFilm({})", film);
        jdbcTemplate.update(
                "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)",
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId());
        Film thisFilm = jdbcTemplate.queryForObject(
                "SELECT film_id, name, description, release_date, duration, mpa_id FROM films WHERE name=? "
                        + "AND description=? AND release_date=? AND duration=? AND mpa_id=?",
                new FilmMapper(), film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId());
        log.trace("The movie {} was added to the data base", thisFilm);
        return thisFilm;
    }

    @Override
    public Film updateFilm(Film film) {
        log.debug("updateFilm({}).", film);
        jdbcTemplate.update(
                "UPDATE films SET name=?, description=?, release_date=?, duration=?, mpa_id=? WHERE film_id=?",
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        Film thisFilm = getFilmById(film.getId());
        log.trace("The movie {} was updated in the data base", thisFilm);
        return thisFilm;
    }

    @Override
    public Film getFilmById(Long id) {
        log.debug("getFilmById({})", id);
        Film thisFilm = jdbcTemplate.queryForObject(
                "SELECT film_id, name, description, release_date, duration, mpa_id FROM films WHERE film_id=?",
                new FilmMapper(), id);
        log.trace("The movie {} was returned", thisFilm);
        return thisFilm;
    }

    @Override
    public List<Film> getFilms() {
        log.debug("getFilms()");
        String sqlQuery = "SELECT films.film_id, films.name, film_mpa.mpa_id, film_mpa.mpa_rating, "
                + "films.description, films.release_date, films.duration, COUNT(likes.user_id) likes "
                + "FROM films LEFT JOIN film_mpa ON films.mpa_id = film_mpa.mpa_id "
                + "LEFT JOIN likes ON films.film_id = likes.film_id "
                + "GROUP BY films.film_id "
                + "ORDER BY COUNT(likes.user_id) DESC";

        final Map<Long, Film> films = new HashMap<>();
        jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            long filmId = rs.getLong("film_id");
            Film film = films.get(filmId);

            if (film == null) {
                film = Film.builder()
                        .id(rs.getLong("film_id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getDate("release_date").toLocalDate())
                        .duration(rs.getLong("duration"))
                        .mpa(rowMapperForMpa(rs))
                        .build();

                films.put(filmId, film);
            }
            return film;
        });

        addGenresToFilm(films);

        return films.values().stream().collect(Collectors.toList());
    }

    @Override
    public void addLike(Long filmId, Long userId) {

    }

    @Override
    public void removeLike(Long filmId, Long userId) {

    }

    @Override
    public int getLikesQuantity(Long filmId) {
        return 0;
    }

    @Override
    public boolean isContains(Long id) {
        log.debug("isContains({})", id);
        try {
            getFilmById(id);
            log.trace("The movie with id {} was found", id);
            return true;
        } catch (EmptyResultDataAccessException exception) {
            log.trace("No information has been found for id {}", id);
            return false;
        }
    }

    @Override
    public void addGenres(Long filmId, Set<Genre> genres) {
        log.debug("addGenres({}, {})", filmId, genres);
        for (Genre genre : genres) {
            jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)", filmId, genre.getId());
            log.trace("Genres were added to a movie {}", filmId);
        }
    }

    @Override
    public void updateGenres(Long filmId, Set<Genre> genres) {
        log.debug("updateGenres({}, {})", filmId, genres);
        deleteGenres(filmId);
        addGenres(filmId, genres);
    }

    @Override
    public Set<Genre> getGenres(Long filmId) {
        log.debug("getGenres({})", filmId);
        Set<Genre> genres = new HashSet<>(jdbcTemplate.query(
                "SELECT f.genre_id, g.genre_type FROM film_genre AS f " +
                        "LEFT OUTER JOIN genre AS g ON f.genre_id = g.genre_id WHERE f.film_id=? ORDER BY g.genre_id",
                new GenreMapper(), filmId));
        log.trace("Genres for the movie with id {} were returned", filmId);
        return genres;
    }

    @Override
    public void deleteGenres(Long filmId) {
        log.debug("deleteGenres({})", filmId);
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id=?", filmId);
        log.trace("All genres were removed for a movie with id {}", filmId);
    }

    private void addGenresToFilm(Map<Long, Film> filmsMap) {
        Collection<Long> filmId = filmsMap.keySet();

        String sqlQuery = "SELECT film_genre.film_id, genre.genre_id, genre.genre_type "
                + "FROM film_genre "
                + "JOIN genre ON film_genre.genre_id = genre.genre_id "
                + "WHERE film_genre.film_id IN (:filmId)";

        SqlParameterSource parameters = new MapSqlParameterSource("filmId", filmId);

        namedParameterJdbcTemplate.query(sqlQuery, parameters, rs -> {
            Film film = filmsMap.get(rs.getLong("film_id"));
            Set<Genre> genres = new HashSet<>();
            genres.add(Genre.builder()
                    .id(rs.getInt("genre_id"))
                    .name(rs.getString("genre_type"))
                    .build());
            film.setGenres(genres);
            filmsMap.put(film.getId(), film);
        });
    }

    private Mpa rowMapperForMpa(ResultSet rs) throws SQLException {
        return Mpa.builder()
                .id(rs.getInt("mpa_id"))
                .name(rs.getString("mpa_rating"))
                .build();
    }
}
