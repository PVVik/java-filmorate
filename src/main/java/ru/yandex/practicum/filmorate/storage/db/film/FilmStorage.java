package ru.yandex.practicum.filmorate.storage.db.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Set;

public interface FilmStorage {

    Film createFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmById(Long id);

    List<Film> getFilms();

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    int getLikesQuantity(Long filmId);

    boolean isContains(Long id);

    void addGenres(Long filmId, Set<Genre> genres);

    void updateGenres(Long filmId, Set<Genre> genres);

    Set<Genre> getGenres(Long filmId);

    void deleteGenres(Long filmId);
}
