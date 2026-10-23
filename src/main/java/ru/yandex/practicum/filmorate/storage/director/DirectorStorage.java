package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface DirectorStorage {

    Director create(Director director);

    Director update(Director director);

    List<Director> getAll();

    Director getById(long id);

    List<Director> getDirectorsByFilmId(long filmId);

    void delete(long id);

    Set<Long> addFilmDirectors(long filmId, Set<Long> directors);

    void removeFilmDirectors(long filmId);

    Set<Long> getExistingIds(Collection<Long> ids);

}
