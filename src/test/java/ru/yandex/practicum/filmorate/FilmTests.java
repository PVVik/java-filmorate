package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmRowMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class})
class FilmTests {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private JdbcTemplate jdbc;

    private static final LocalDate RELEASE_DATE = LocalDate.of(2000, 1, 1);

    @Test
    @DisplayName("Метод должен успешно добавить фильм и присвоить ID")
    public void addFilm_shouldAddFilm() {
        Film film = createFilm("Test Film");
        Film saved = filmStorage.addFilm(film);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Film");
        assertThat(saved.getDuration()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Метод должен обновить данные фильма")
    public void updateFilm_shouldUpdateFilm() {
        Film saved = filmStorage.addFilm(createFilm("Old Name"));
        saved.setName("New Name");
        saved.setDescription("Updated description");
        saved.setDuration(120L);

        Film updated = filmStorage.updateFilm(saved);

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getDuration()).isEqualTo(120L);
    }

    @Test
    @DisplayName("Метод должен вернуть список всех фильмов")
    public void getFilms_shouldReturnAllFilms() {
        filmStorage.addFilm(createFilm("Film 1"));
        filmStorage.addFilm(createFilm("Film 2"));

        List<Film> films = filmStorage.getFilms();

        boolean hasF1 = films.stream().anyMatch(f -> f.getName().equals("Film 1"));
        boolean hasF2 = films.stream().anyMatch(f -> f.getName().equals("Film 2"));

        assertThat(hasF1).isTrue();
        assertThat(hasF2).isTrue();
    }

    @Test
    @DisplayName("Метод должен найти фильм по ID")
    public void getFilmById_shouldReturnFilm() {
        Film saved = filmStorage.addFilm(createFilm("Target Film"));

        Film found = filmStorage.getFilmById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("Target Film");
    }

    @Test
    @DisplayName("Метод должен выбросить NotFoundException для несуществующего ID")
    public void getFilmById_shouldThrow_whenFilmNotFound() {
        assertThatThrownBy(() -> filmStorage.getFilmById(999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Метод getPopularFilms должен вернуть фильмы, отсортированные по количеству лайков")
    public void getPopularFilms_shouldSortByLikes() {
        long u1 = createTestUser("user1@email.ru", "login1", "Name 1", LocalDate.of(1999, 1, 1));
        long u2 = createTestUser("user2@email.ru", "login2", "Name 2", LocalDate.of(1999, 2, 1));
        long u3 = createTestUser("user3@email.ru", "login3", "Name 3", LocalDate.of(1999, 3, 1));

        Film f1 = filmStorage.addFilm(createFilm("Popular"));
        Film f2 = filmStorage.addFilm(createFilm("Less Popular"));

        filmStorage.addLike(f1.getId(), u1);
        filmStorage.addLike(f1.getId(), u2);
        filmStorage.addLike(f2.getId(), u3);

        List<Film> popular = filmStorage.getPopularFilms(2);

        assertEquals(2, popular.size());
        assertThat(popular.get(0).getId()).isEqualTo(f1.getId());
    }

    @Test
    @DisplayName("Метод addLike должен добавить лайк и не дублировать логику в тесте")
    public void addLike_shouldInsertLike() {
        Film film = filmStorage.addFilm(createFilm("Like Test"));
        long u1 = createTestUser("user1@email.ru", "login1", "Name 1", LocalDate.of(1999, 1, 1));

        filmStorage.addLike(film.getId(), u1);

        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ?",
                Long.class, film.getId());

        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("Метод deleteLike должен удалить лайк")
    public void deleteLike_shouldRemoveLike() {
        Film film = filmStorage.addFilm(createFilm("Delete Like Test"));
        long u1 = createTestUser("user1@email.ru", "login1", "Name 1", LocalDate.of(1999, 1, 1));

        filmStorage.addLike(film.getId(), u1);

        Long beforeDelete = jdbc.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Long.class, film.getId(), u1);
        assertThat(beforeDelete).isEqualTo(1L);

        filmStorage.deleteLike(film.getId(), u1);

        Long afterDelete = jdbc.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Long.class, film.getId(), u1);
        assertThat(afterDelete).isEqualTo(0L);
    }

    private long createTestUser(String email, String login, String name, LocalDate birthday) {
        String sql = "INSERT INTO users (email, login, user_name, birthday) VALUES (?, ?, ?, ?)";
        jdbc.update(sql, email, login, name, birthday);

        return jdbc.queryForObject("SELECT MAX(user_id) FROM users", Long.class);
    }

    private Film createFilm(String name) {
        return Film.builder()
                .name(name)
                .description("Description of " + name)
                .releaseDate(RELEASE_DATE)
                .duration(100L)
                .mpaId(1L)
                .build();
    }
}
