package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        film.addLke(user.getId());
        filmStorage.updateFilm(film);
        log.info("Добавили лайк пользователя с id {} к фильму с id {}", userId, filmId);
    }

    public void deleteLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        film.deleteLike(user.getId());
        filmStorage.updateFilm(film);
        log.info("Удалили лайк пользователя с id {} с фильма с id {}", userId, filmId);
    }

    public List<Film> getPopularFilms(long count) {
        log.info("Получили список популярных фильмов");
        return filmStorage.getFilms().stream()
                .sorted(Film::compareTo)
                .limit(count)
                .collect(Collectors.toList());
    }
}
