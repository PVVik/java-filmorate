package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {

    LocalDate MIN_DATE_FOR_FILM = LocalDate.of(1895, Month.DECEMBER, 28);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilms();

    Film getFilmById(long filmId);

    List<Film> getPopularFilms(long count);

    List<Film> getPopularFilms(long count, Long genreId, Integer year);

    List<Film> getCommonFilms(long userId, long friendId);

    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);

    void deleteFilm(long id);

    List<Film> getByDirectorId(long id, String sortBy, String sortOrder);

    Set<Long> findLikedFilmIdsByUser(long userId);

    Optional<Long> findMostSimilarUserId(long userId);

    List<Film> search(String query, List<String> by);

}
