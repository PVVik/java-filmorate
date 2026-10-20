package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorRowMapper;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({DirectorDbStorage.class, DirectorRowMapper.class})
class DirectorTests {

    @Autowired
    private DirectorDbStorage directorStorage;

    @Autowired
    private JdbcTemplate jdbc;

    private static final String DIRECTOR_NAME_INITIAL = "Initial Director";
    private static final String DIRECTOR_NAME_UPDATED = "Updated Director";

    @Test
    @DisplayName("create: должен успешно добавить режиссёра с корректным ID")
    public void createDirector_shouldAddDirector() {
        Director director = Director.builder()
                .name(DIRECTOR_NAME_INITIAL)
                .build();

        Director saved = directorStorage.create(director);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isGreaterThan(0);
        assertThat(saved.getName()).isEqualTo(DIRECTOR_NAME_INITIAL);
    }

    @Test
    @DisplayName("update: должен обновить имя режиссёра")
    public void updateDirector_shouldUpdateDirector() {
        Director initial = Director.builder().name(DIRECTOR_NAME_INITIAL).build();
        Director saved = directorStorage.create(initial);

        saved.setName(DIRECTOR_NAME_UPDATED);

        Director updated = directorStorage.update(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getName()).isEqualTo(DIRECTOR_NAME_UPDATED);
    }

    @Test
    @DisplayName("getAll: должен вернуть список всех режиссёров")
    public void getAll_shouldReturnAllDirectors() {
        Director d1 = Director.builder().name("Director A").build();
        Director d2 = Director.builder().name("Director B").build();

        directorStorage.create(d1);
        directorStorage.create(d2);

        List<Director> all = directorStorage.getAll();

        assertThat(all).hasSize(2);
        assertThat(all.get(0).getName()).isEqualTo("Director A");
        assertThat(all.get(1).getName()).isEqualTo("Director B");
    }

    @Test
    @DisplayName("getById: должен вернуть режиссёра по ID")
    public void getById_shouldReturnDirector() {
        Director initial = Director.builder().name(DIRECTOR_NAME_INITIAL).build();
        Director saved = directorStorage.create(initial);

        Director found = directorStorage.getById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo(DIRECTOR_NAME_INITIAL);
    }

    @Test
    @DisplayName("getById: должен выбросить NotFoundException для несуществующего ID")
    public void getById_shouldThrow_whenDirectorNotFound() {
        assertThatThrownBy(() -> directorStorage.getById(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("delete: должен удалить режиссёра из БД")
    public void delete_shouldRemoveDirector() {
        Director initial = Director.builder().name(DIRECTOR_NAME_INITIAL).build();
        Director saved = directorStorage.create(initial);

        directorStorage.delete(saved.getId());

        assertThatThrownBy(() -> directorStorage.getById(saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("addFilmDirectors: должен добавить связи film_directors и вернуть набор ID режиссёров")
    public void addFilmDirectors_shouldInsertLinks() {
        long filmId = createTestFilm("Test Film", "Desc", LocalDate.of(2000, 1, 1), 120L);

        Director d1 = Director.builder().name("Dir 1").build();
        Director d2 = Director.builder().name("Dir 2").build();
        long id1 = directorStorage.create(d1).getId();
        long id2 = directorStorage.create(d2).getId();

        Set<Long> directors = new HashSet<>();
        directors.add(id1);
        directors.add(id2);

        Set<Long> result = directorStorage.addFilmDirectors(filmId, directors);

        assertThat(result).containsExactlyInAnyOrder(id1, id2);

        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_directors WHERE film_id = ?",
                Long.class, filmId);

        assertThat(count).isEqualTo(2L);
    }

    private long createTestFilm(String name, String description, LocalDate releaseDate, long duration) {
        String sql = "INSERT INTO films (film_name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, (SELECT COALESCE(MAX(mpa_id), 1) FROM mpa))";
        jdbc.update(sql, name, description, releaseDate, duration);
        return jdbc.queryForObject("SELECT MAX(film_id) FROM films", Long.class);
    }
}

