package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Set;

public interface DirectorStorage {

    Director create(Director director);

    Director update(Director director);

    List<Director> getAll();

    Director getById(long id);

    void delete(long id);

    Set<Long> addFilmDirectors(long filmId, Set<Long> directors);

}
