package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Set;

public interface GenreStorage {

    List<Genre> getGenres();

    Genre getGenreById(long genreId);

    Set<Long> addFilmGenres(long filmId, Set<Long> genre);
}
