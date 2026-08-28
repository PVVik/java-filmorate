package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public interface FilmStorage {

    LocalDate MIN_DATE_FOR_FILM = LocalDate.of(1895, Month.DECEMBER, 28);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilms();

    Film getFilmById(long filmId);

    List<Film> getPopularFilms(long count);

}
