package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;

public class FilmControllerTest {
    private final FilmController controller = new FilmController();
    private final Film film = Film.builder()
            .id(1)
            .name("name")
            .description("description")
            .releaseDate(LocalDate.of(2023, Month.AUGUST, 16))
            .duration(100)
            .build();

    @Test
    void shouldAddFilm() throws ValidationException {
        Film thisFilm = new Film(1, "name", "description",
                LocalDate.of(2023, Month.AUGUST, 16), 100);
        controller.addFilm(thisFilm);

        Assertions.assertEquals(film, thisFilm);
        Assertions.assertEquals(1, controller.getAllFilms().size());
    }

    @Test
    void shouldUpdateMovieData() throws ValidationException {
        Film thisFilm = new Film(1, "name", "description1",
                LocalDate.of(2023, Month.AUGUST, 16), 100);
        controller.addFilm(film);
        controller.updateFilm(thisFilm);

        Assertions.assertEquals("description1", thisFilm.getDescription());
        Assertions.assertEquals(1, controller.getAllFilms().size());
    }

    @Test
    void shouldNotAddAMovieWithAnEmptyName() {
        film.setName("");

        Assertions.assertThrows(ValidationException.class, () -> controller.addFilm(film));
        Assertions.assertEquals(0, controller.getAllFilms().size());
    }

    @Test
    void shouldNotAddAMovieWithDescriptionMoreThan200() {
        film.setDescription("..................................................................................." +
                "..............................................................................................." +
                "................................................................................................");

        Assertions.assertThrows(ValidationException.class, () -> controller.addFilm(film));
        Assertions.assertEquals(0, controller.getAllFilms().size());
    }

    @Test
    void shouldNotAddAMovieWithDateReleaseMoreThan1895() {
        film.setReleaseDate(LocalDate.of(1891, 2, 2));

        Assertions.assertThrows(ValidationException.class, () -> controller.addFilm(film));
        Assertions.assertEquals(0, controller.getAllFilms().size());
    }

    @Test
    void shouldNotAddAMovieIfDurationIsMoreThan0() {
        film.setDuration(-15);

        Assertions.assertThrows(ValidationException.class, () -> controller.addFilm(film));
        Assertions.assertEquals(0, controller.getAllFilms().size());
    }

}