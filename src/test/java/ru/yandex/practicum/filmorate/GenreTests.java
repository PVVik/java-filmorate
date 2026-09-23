package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreRowMapper;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreDbStorage.class, GenreRowMapper.class})
class GenreTests {

    @Autowired
    private GenreDbStorage genreStorage;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("Метод должен вернуть список всех жанров")
    public void getGenres_shouldReturnAllGenres() {
        List<Genre> genres = genreStorage.getGenres();

        assertThat(genres).isNotEmpty();
    }

    @Test
    @DisplayName("Метод должен найти жанр по id")
    public void getGenreById_shouldReturnGenre() {
        Genre genre = genreStorage.getGenreById(1);

        assertThat(genre.getId()).isEqualTo(1);
        assertThat(genre.getName()).isNotNull();
    }

    @Test
    @DisplayName("Метод должен бросить NotFoundException для несуществующего id")
    public void getGenreById_shouldThrow_whenGenreNotFound() {
        assertThatThrownBy(() -> genreStorage.getGenreById(999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Метод должен добавить жанры фильму и вернуть их ID")
    public void addFilmGenres_shouldAddGenres() {
        long filmId = createFilmForTest();
        Set<Long> genreIds = new HashSet<>(Set.of(1L, 2L));

        Set<Long> result = genreStorage.addFilmGenres(filmId, genreIds);

        assertThat(result).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("Метод должен вернуть жанры фильма, отсортированные по id")
    public void getGenresByFilmId_shouldReturnSortedGenres() {
        long filmId = createFilmForTest();
        genreStorage.addFilmGenres(filmId, Set.of(3L, 1L, 2L));

        Set<Genre> genres = genreStorage.getGenresByFilmId(filmId);

        assertThat(genres).hasSize(3);
        assertThat(genres).extracting(Genre::getId).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("Метод должен вернуть пустой сет, если у фильма нет жанров")
    public void getGenresByFilmId_shouldReturnEmpty_whenNoGenres() {
        long filmId = createFilmForTest();

        Set<Genre> genres = genreStorage.getGenresByFilmId(filmId);

        assertThat(genres).isEmpty();
    }

    private long createFilmForTest() {
        jdbc.update("INSERT INTO films (film_name, description, release_date, duration, mpa_id) " +
                        "VALUES (?, ?, ?, ?, ?)",
                "Test Film", "Description", LocalDate.of(2000, 1, 1), 100, 1);
        return jdbc.queryForObject("SELECT film_id FROM films WHERE film_name = 'Test Film'",
                Long.class);
    }
}