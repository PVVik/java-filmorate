package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film addFilm(Film film) throws ValidationException;

    Film updateFilm(Film film) throws ValidationException;

    List<Film> getAllFilms();

    Film getFilmById(int id);

    void deleteFilms();

    List<Film> getPopularFilms(Integer count);
}
