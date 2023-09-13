package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    public void addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);
        userService.getUserById(userId);
        if (film == null) {
            throw new ObjectNotFoundException("Такого фильма не существует.");
        }
        film.addLike(userId);
        log.info("Поставлен лайк пользователем '{}' на фильм '{}'.", userId, filmId);
    }

    public void deleteLike(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        userService.getUserById(userId);
        if (film == null) {
            throw new ObjectNotFoundException("Такого фильма не существует.");
        }
        film.removeLike(userId);
        log.info("Удалён лайк пользователя '{}' с фильма '{}'.", userId, filmId);
    }

    public List<Film> getPopularFilms(Integer count) {
        return filmStorage.getPopularFilms(count);
    }

    public Film addFilm(Film film) throws ValidationException {
        filmValidate(film);
        return filmStorage.addFilm(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film updateFilm(Film film) throws ValidationException {
        filmValidate(film);
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id);
    }

    private void filmValidate(Film film) throws ValidationException {
        if (film.getReleaseDate() == null ||
                film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
            throw new ValidationException("Введена неверная дата релиза.");
        }
        if (film.getName().isEmpty() || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым.");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть больше нуля.");
        }
        if (film.getDescription().length() > 200 || film.getDescription().length() == 0) {
            throw new ValidationException("Неверное количество символов в описании.");
        }
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
    }
}
