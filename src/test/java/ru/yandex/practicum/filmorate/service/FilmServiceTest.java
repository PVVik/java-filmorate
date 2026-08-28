package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FilmServiceTest {

    private FilmService filmService;
    private FilmStorage filmStorage;
    private UserService userService;
    private final Film film1 = Film.builder().name("name1").description("des1").duration(101L)
            .releaseDate(LocalDate.of(1999, Month.MAY, 1)).build();
    private final Film film2 = Film.builder().name("name2").description("des2").duration(102L)
            .releaseDate(LocalDate.of(1999, Month.MAY, 2)).build();
    private final Film film3 = Film.builder().name("name3").description("des3").duration(103L)
            .releaseDate(LocalDate.of(1999, Month.MAY, 3)).build();
    private final User user1 = User.builder().email("email1@email.ru").login("login1").name("name1")
            .birthday(LocalDate.of(2002, Month.MARCH, 1)).build();
    private final User user2 = User.builder().email("email2@email.ru").login("login2").name("name2")
            .birthday(LocalDate.of(2002, Month.MARCH, 2)).build();

    @BeforeEach
    public void beforeEach() {
        filmStorage = new InMemoryFilmStorage();
        userService = new UserService(new InMemoryUserStorage());
        filmService = new FilmService(filmStorage, userService);
    }

    @Test
    @DisplayName("Метод должен успешно добавить лайк")
    public void addLike_shouldAddLike() {
        filmStorage.addFilm(film1);
        userService.addUser(user1);
        filmService.addLike(film1.getId(), user1.getId());

        Assertions.assertEquals(1, film1.getLikes().size());
        Assertions.assertTrue(film1.getLikes().contains(1L));
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку, так как фильм не найден")
    public void addLike_shouldGetErrorFilmNotExists() {
        userService.addUser(user1);

        Assertions.assertThrows(NotFoundException.class, () -> filmService.addLike(1, user1.getId()));
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку, так как пользователь не найден")
    public void addLike_shouldGetErrorUserNotExists() {
        filmStorage.addFilm(film1);

        Assertions.assertThrows(NotFoundException.class, () -> filmService.addLike(film1.getId(), 1));
    }

    @Test
    @DisplayName("Метод должен успешно удалить лайк")
    public void deleteLike_shouldDeleteLike() {
        filmStorage.addFilm(film1);
        userService.addUser(user1);
        filmService.addLike(film1.getId(), user1.getId());
        filmService.deleteLike(film1.getId(), user1.getId());

        Assertions.assertEquals(0, film1.getLikes().size());
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку, так как фильм не найден")
    public void deleteLike_shouldGetErrorFilmNotExists() {
        userService.addUser(user1);

        Assertions.assertThrows(NotFoundException.class, () -> filmService.deleteLike(1, user1.getId()));
    }

    @Test
    @DisplayName("Метод должен вернуть ошибку, так как пользователь не найден")
    public void deleteLike_shouldGetErrorUserNotExists() {
        filmStorage.addFilm(film1);

        Assertions.assertThrows(NotFoundException.class, () -> filmService.deleteLike(film1.getId(), 1));
    }


    @Test
    @DisplayName("Метод должен вернуть корректный список фильмов")
    public void getPopularFilms_shouldGetListFilms() {
        Film addedFilm1 = filmStorage.addFilm(film1);
        Film addedFilm2 = filmStorage.addFilm(film2);
        filmStorage.addFilm(film3);

        userService.addUser(user1);
        userService.addUser(user2);

        film1.addLke(user1.getId());
        film1.addLke(user2.getId());
        film2.addLke(user1.getId());

        List<Film> popularFilms = filmService.getPopularFilms(2);

        Assertions.assertEquals(2, popularFilms.size());
        Assertions.assertEquals(addedFilm1, popularFilms.get(0));
        Assertions.assertEquals(addedFilm2, popularFilms.get(1));
    }

    @Test
    @DisplayName("Метод должен вернуть пустой список")
    public void getPopularFilms_shouldGetEmptyList() {
        Assertions.assertEquals(new ArrayList<>(), filmService.getPopularFilms(10));
    }
}
