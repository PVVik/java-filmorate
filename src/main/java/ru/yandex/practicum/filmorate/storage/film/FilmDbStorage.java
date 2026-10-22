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

    private final ResultSetExtractor<List<Film>> filmWithGenresExtractorAndDirectors = rs -> {
        Map<Long, Film> filmMap = new LinkedHashMap<>();
        Set<Long> genreIds = new LinkedHashSet<>();
        Set<Long> directorIds = new LinkedHashSet<>();

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
                        .directors(new LinkedHashSet<>())
                        .likes(new HashSet<>())
                        .build();
                filmMap.put(filmId, film);
            }

            long genreId = rs.getLong("genre_id");
            if (!rs.wasNull()) {
                film.getGenres().add(genreId);
            }

            long directorId = rs.getLong("director_id");
            if (!rs.wasNull()) {
                film.getDirectors().add(directorId);
            }
        }

        return new ArrayList<>(filmMap.values());
    };

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
                "f.duration, f.mpa_id, fg.genre_id, fd.director_id " +
                "FROM films AS f " +
                "LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id " +
                "LEFT JOIN film_directors AS fd ON fd.film_id = f.film_id";

        log.debug("SELECT f.film_id, f.film_name, f.description, f.release_date, " +
                "f.duration, f.mpa_id, fg.genre_id " +
                "FROM films AS f " +
                "LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id " +
                "LEFT JOIN film_directors AS fd ON fd.film_id = f.film_id");

        return jdbc.query(query, filmWithGenresExtractorAndDirectors);
    }

    @Override
    public Film getFilmById(long filmId) {
        String query = "SELECT f.film_id, f.film_name, f.description, f.release_date, " +
                "f.duration, f.mpa_id, fg.genre_id, fd.director_id FROM films AS f " +
                "LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id " +
                "LEFT JOIN film_directors AS fd ON fd.film_id = f.film_id " +
                "WHERE f.film_id = ?";

        log.debug("SELECT f.film_id, f.film_name, f.description, f.release_date, " +
                "f.duration, f.mpa_id, fg.genre_id, fd.director_id FROM films AS f " +
                "LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id " +
                "LEFT JOIN film_directors AS fd ON fd.film_id = f.film_id " +
                "WHERE f.film_id = {}", filmId);

        var film = jdbc.query(query, filmWithGenresExtractorAndDirectors, filmId);

        if (film == null || film.isEmpty()) {
            throw new NotFoundException(String.format("Фильм с id %d не был найден", filmId));
        } else return film.getFirst();
    }

    @Override
    public List<Film> getPopularFilms(long count, Long genreId, Integer year) {
        StringBuilder sql = new StringBuilder("""
                SELECT f.film_id, f.film_name, f.description, f.release_date,
                       f.duration, f.mpa_id, fg.genre_id, fd.director_id,
                       COUNT(DISTINCT l.user_id) AS like_count
                FROM films AS f
                LEFT JOIN likes AS l ON l.film_id = f.film_id
                LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id
                LEFT JOIN film_directors AS fd ON fd.film_id = f.film_id
                """);

        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            sql.append(" WHERE fg.genre_id = ?");
            params.add(genreId);
        }

        if (year != null) {
            sql.append(genreId != null ? " AND " : " WHERE ");
            sql.append(" EXTRACT(YEAR FROM f.release_date) = ? ");
            params.add(year);
        }

        sql.append("""
                GROUP BY f.film_id, f.film_name, f.description, f.release_date,
                         f.duration, f.mpa_id, fg.genre_id, fd.director_id
                ORDER BY like_count DESC
                LIMIT ?
                """);
        params.add(count);

        log.debug("SQL: {}", sql.toString());

        return jdbc.query(sql.toString(), filmWithGenresExtractorAndDirectors, params.toArray());
    }

    @Override
    public List<Film> getPopularFilms(long count) {
        return getPopularFilms(count, null, null);
    }

    @Override
    public List<Film> getCommonFilms(long userId, long friendId) {
        String query = """
                SELECT f.film_id, f.film_name, f.description, f.release_date,
                       f.duration, f.mpa_id, fg.genre_id, fd.director_id,
                       total_likes.like_count
                FROM films AS f
                JOIN (
                    SELECT film_id, COUNT(DISTINCT user_id) AS like_count
                    FROM likes
                    GROUP BY film_id
                ) AS total_likes ON total_likes.film_id = f.film_id
                JOIN likes AS l ON l.film_id = f.film_id AND l.user_id IN (?, ?)
                LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id
                LEFT JOIN film_directors AS fd ON fd.film_id = f.film_id
                GROUP BY f.film_id, f.film_name, f.description, f.release_date,
                         f.duration, f.mpa_id, total_likes.like_count, fg.genre_id,
                         fd.director_id
                HAVING COUNT(DISTINCT l.user_id) = 2
                ORDER BY total_likes.like_count DESC
                """;

        log.debug("Executing getCommonFilms for users {}, {}", userId, friendId);
        return jdbc.query(query, filmWithGenresExtractorAndDirectors, userId, friendId);
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

    @Override
    public void deleteFilm(long filmId) {
        log.debug("Удаление фильма с id {}", filmId);
        delete("DELETE FROM films WHERE film_id = ?", filmId);
    }

    @Override
    public List<Film> getByDirectorId(long id) {
        String query = """
                SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration,
                    f.mpa_id, fg.genre_id, fd.director_id,
                    (
                        SELECT COUNT(DISTINCT l.user_id)
                        FROM likes AS l
                        WHERE l.film_id = f.film_id
                    ) AS like_count
                FROM films AS f
                JOIN film_directors AS fd ON fd.film_id = f.film_id
                LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id
                WHERE fd.director_id = ?
                ORDER BY like_count DESC, f.release_date ASC;
                """;

        log.debug("""
                SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration,
                    f.mpa_id, fg.genre_id, fd.director_id,
                    (
                        SELECT COUNT(DISTINCT l.user_id)
                        FROM likes AS l
                        WHERE l.film_id = f.film_id
                    ) AS like_count
                FROM films AS f
                JOIN film_directors AS fd ON fd.film_id = f.film_id
                LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id
                WHERE fd.director_id = {}
                ORDER BY like_count DESC, f.release_date ASC;
                """, id);

        return jdbc.query(query, filmWithGenresExtractorAndDirectors, id);
    }

    @Override
    public Set<Long> findLikedFilmIdsByUser(long userId) {
        String sql = "SELECT film_id FROM likes WHERE user_id = ?";
        log.debug("SELECT film_id FROM likes WHERE user_id = {}", userId);
        return new HashSet<>(jdbc.queryForList(sql, Long.class, userId));
    }

    @Override
    public Optional<Long> findMostSimilarUserId(long userId) {
        String sql = """
                SELECT l2.user_id
                FROM likes AS l1
                JOIN likes l2 ON l1.film_id = l2.film_id
                WHERE
                    l1.user_id = ?
                    AND l2.user_id != ?
                GROUP BY l2.user_id
                ORDER BY COUNT(*) DESC
                LIMIT 1
                """;

        List<Long> result = jdbc.queryForList(sql, Long.class, userId, userId);

        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    public List<Film> search(String query, List<String> by) {
        StringBuilder sql = new StringBuilder("""
                SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration,
                    f.mpa_id, fg.genre_id, fd.director_id,
                    (
                        SELECT COUNT(DISTINCT l.user_id)
                        FROM likes AS l
                        WHERE l.film_id = f.film_id
                    ) AS like_count
                FROM films AS f
                LEFT JOIN film_directors AS fd ON fd.film_id = f.film_id
                LEFT JOIN film_genres AS fg ON fg.film_id = f.film_id
                LEFT JOIN directors as d ON fd.director_id = d.director_id
                """);
        List<String> params = new ArrayList<>();

        String cleanQuery = query.trim().toLowerCase();
        String searchTerm = "%" + cleanQuery + "%";

        if (by.size() == 2) {
            sql.append(" WHERE LOWER(d.director_name) LIKE ? OR LOWER(f.film_name) LIKE ?");
            params.add(searchTerm);
            params.add(searchTerm);
        } else {
            if (by.contains("director")) {
                sql.append(" WHERE LOWER(d.director_name) LIKE ?");
                params.add(searchTerm);
            } else if (by.contains("title")) {
                sql.append(" WHERE LOWER(f.film_name) LIKE ?");
                params.add(searchTerm);
            }
        }
        sql.append(" ORDER BY like_count DESC");

        log.debug(sql.toString());

        return jdbc.query(sql.toString(), filmWithGenresExtractorAndDirectors, params.toArray());
    }
}