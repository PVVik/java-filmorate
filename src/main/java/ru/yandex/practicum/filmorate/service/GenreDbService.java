package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.genre.GenreDao;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class GenreDbService {
    private final GenreDao genreDao;

    public Genre getGenreById(Integer id) {
        if (id == null || !genreDao.isContains(id)) {
            throw new ObjectNotFoundException("Negative or empty id was passed");
        }
        return genreDao.getGenreById(id);
    }

    public Collection<Genre> getGenres() {
        return genreDao.getGenres();
    }
}
