package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    public void addLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userService.getUserById(userId);

        film.addLke(user.getId());
        filmStorage.updateFilm(film);
        log.info("Добавили лайк пользователя с id {} к фильму с id {}", userId, filmId);
    }

    public void deleteLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId);

        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException(String.format("В списке лайков нет пользователя с id %d", userId));
        }

        User user = userService.getUserById(userId);

        film.deleteLike(user.getId());
        filmStorage.updateFilm(film);
        log.info("Удалили лайк пользователя с id {} с фильма с id {}", userId, filmId);
    }

    public List<Film> getPopularFilms(long count) {
        return filmStorage.getPopularFilms(count);
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(long filmId) {
        return filmStorage.getFilmById(filmId);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }
}
