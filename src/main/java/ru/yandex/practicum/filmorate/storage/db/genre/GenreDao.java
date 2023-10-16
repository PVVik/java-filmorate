package ru.yandex.practicum.filmorate.storage.db.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreDao {

    Genre getGenreById(Integer id);

    List<Genre> getGenres();

    boolean isContains(Integer id);
}
