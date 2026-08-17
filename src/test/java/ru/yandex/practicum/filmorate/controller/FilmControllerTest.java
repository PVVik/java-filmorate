package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

@Slf4j
public class FilmControllerTest {

    private FilmController filmController;
    private final Film rightFilm = Film.builder().name("name").description("description")
            .releaseDate(LocalDate.of(2000, Month.DECEMBER, 20)).duration(200L).build();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @BeforeEach
    public void beforeEach() {
        filmController = new FilmController();
    }

    @Test
    @DisplayName("Метод должен создать фильм при корректных данных")
    public void addFilm_shouldAddFilmWithCorrectData() {
        Film addedFilm = filmController.addFilm(rightFilm);

        Assertions.assertEquals(1, addedFilm.getId());
        Assertions.assertEquals(rightFilm, addedFilm);

    }

    @Test
    @DisplayName("Метод должен вернуть ошибку при пустом имени")
    public void addFilm_shouldGetErrorWithoutName() {
        Film film = Film.builder().description("description").build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("Название фильма не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку при некорректном имени")
    public void addFilm_shouldGetErrorWithWrongName() {
        Film film = Film.builder().name("  ").build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("Название фильма не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку при некорректном описании")
    public void addFilm_shouldGetErrorWithWrongDescription() {
        Film film = Film.builder().name("name").description("01".repeat(101)).build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("Длина описания не может быть больше 200 знаков",
                violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку при пустом описании")
    public void addFilm_shouldGetErrorWithEmptyDescription() {
        Film film = Film.builder().name("name")
                .releaseDate(LocalDate.of(2000, Month.DECEMBER, 12)).build();

        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку при некорректной дате выпуска")
    public void addFilm_shouldGetErrorWithWrongReleaseDate() {
        Film film = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(1000, Month.DECEMBER, 12)).build();

        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку при пустой дате выпуска")
    public void addFilm_shouldGetErrorWithEmptyReleaseDate() {
        Film film = Film.builder().name("name").description("description").duration(200L).build();

        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку при некорректной длительности")
    public void addFilm_shouldGetErrorWithWrongDuration() {
        Film film = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(2000, Month.DECEMBER, 12)).duration(-100L).build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals("Продолжительность фильма может быть только положительной",
                violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку при пустой длительности")
    public void addFilm_shouldGetErrorWithEmptyDuration() {
        Film film = Film.builder().name("name").description("description")
                .releaseDate(LocalDate.of(2000, Month.DECEMBER, 12)).build();

        Assertions.assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    @DisplayName("Метод должен успешно обновить название")
    public void updateFilm_shouldUpdateName() {
        filmController.addFilm(rightFilm);
        Film film = filmController.updateFilm(Film.builder().id(1).name("newName").build());

        Assertions.assertEquals("newName", film.getName());
    }

    @Test
    @DisplayName("Метод должен успешно обновить описание")
    public void updateFilm_shouldUpdateDescription() {
        filmController.addFilm(rightFilm);
        Film film = filmController.updateFilm(Film.builder().id(1).description("newDescription").build());

        Assertions.assertEquals("newDescription", film.getDescription());
    }

    @Test
    @DisplayName("Метод должен успешно обновить дату релиза")
    public void updateFilm_shouldUpdateReleaseDate() {
        filmController.addFilm(rightFilm);
        Film film = filmController.updateFilm(Film.builder().id(1)
                .releaseDate(LocalDate.of(1999, Month.AUGUST, 11)).build());

        Assertions.assertEquals(LocalDate.of(1999, Month.AUGUST, 11), film.getReleaseDate());
    }

    @Test
    @DisplayName("Метод должен успешно обновить продолжительность")
    public void updateFilm_shouldUpdateDuration() {
        filmController.addFilm(rightFilm);
        Film film = filmController.updateFilm(Film.builder().id(1).duration(10L).build());

        Assertions.assertEquals(10L, film.getDuration());
    }

    @Test
    @DisplayName("Метод должен вернуть пустой список, если нет созданных фильмов")
    public void getFilms_shouldGetEmptyList() {
        Assertions.assertEquals(0, filmController.getFilms().size());
    }

    @Test
    @DisplayName("Метод должен вернуть список фильмов")
    public void getFilms_shouldGetList() {
        filmController.addFilm(rightFilm);
        filmController.addFilm(Film.builder().name("name2").description("des")
                .releaseDate(LocalDate.of(2025, Month.APRIL, 9)).duration(100L).build());

        Assertions.assertEquals(2, filmController.getFilms().size());
    }
}
