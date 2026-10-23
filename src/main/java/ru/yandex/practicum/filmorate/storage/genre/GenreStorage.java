package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {

    List<GenreDto> getGenresDto();

    Genre getGenreById(long genreId);

    List<GenreDto> getGenresByFilmId(long filmId);

    List<Long> addFilmGenres(long filmId, List<Long> genres);

    void removeFilmGenres(long filmId);
}
