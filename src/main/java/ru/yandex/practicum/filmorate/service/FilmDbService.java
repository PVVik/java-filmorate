package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ObjectAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.db.genre.GenreDao;
import ru.yandex.practicum.filmorate.storage.db.like.LikeDao;
import ru.yandex.practicum.filmorate.storage.db.mpa.MpaDao;
import ru.yandex.practicum.filmorate.storage.db.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.db.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;

@Slf4j
@Service
@Component
public class FilmDbService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDao genreDao;
    private final MpaDao mpaDao;
    private final LikeDao likeDao;

    @Autowired
    public FilmDbService(@Qualifier("FilmDbStorage") FilmDbStorage filmStorage,
                         @Qualifier("UserDbStorage") UserDbStorage userStorage,
                         GenreDao genreDao,
                         MpaDao mpaDao,
                         LikeDao likeDao) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreDao = genreDao;
        this.mpaDao = mpaDao;
        this.likeDao = likeDao;
    }

    public Film createFilm(Film film) {
        checkIfExists(film);
        validate(film);
        Film thisFilm = filmStorage.createFilm(film);
        filmStorage.addGenres(thisFilm.getId(), film.getGenres());
        thisFilm.setGenres(filmStorage.getGenres(thisFilm.getId()));
        thisFilm.setMpa(mpaDao.getMpaById(thisFilm.getMpa().getId()));
        return thisFilm;
    }

    public Film updateFilm(Film film) {
        checkIfNotExists(film);
        validate(film);
        Film thisFilm = filmStorage.updateFilm(film);
        filmStorage.updateGenres(thisFilm.getId(), film.getGenres());
        thisFilm.setGenres(filmStorage.getGenres(thisFilm.getId()));
        thisFilm.setMpa(mpaDao.getMpaById(thisFilm.getMpa().getId()));
        return thisFilm;
    }

    public Film getFilmById(Long filmId) {
        if (!filmStorage.isContains(filmId)) {
            throw new ObjectNotFoundException("Unable to find a movie with id " + filmId);
        }
        Film film = filmStorage.getFilmById(filmId);
        film.setGenres(filmStorage.getGenres(filmId));
        film.setMpa(mpaDao.getMpaById(film.getMpa().getId()));
        return film;
    }

    public Collection<Film> getFilms() {
        var films = filmStorage.getFilms();
        for (Film film : films) {
            film.setGenres(filmStorage.getGenres(film.getId()));
            film.setMpa(mpaDao.getMpaById(film.getMpa().getId()));
        }
        return films;
    }

    public Collection<Film> getPopularMovies(Integer count) {
        log.debug("getPopularMovies({})", count);
        List<Film> popularMovies = filmStorage.getPopular(count);
        log.trace("These are the most popular movies: {}", popularMovies);
        return popularMovies;
    }

    public void like(Long filmId, Long userId) {
        likeChecker(filmId, userId);
        if (likeDao.isLiked(filmId, userId)) {
            throw new ObjectAlreadyExistsException(format("User with id %d already liked a movie %d", userId, filmId));
        }
        likeDao.like(filmId, userId);
    }

    public void dislike(Long filmId, Long userId) {
        likeChecker(filmId, userId);
        if (!likeDao.isLiked(filmId, userId)) {
            throw new ObjectNotFoundException(format("User with id %d didn't like a movie %d", userId, filmId));
        }
        likeDao.dislike(filmId, userId);
    }

    private void checkIfNotExists(Film film) {
        log.debug("checkIfNotExists({})", film);
        if (film.getId() != null) {
            if (!filmStorage.isContains(film.getId())) {
                throw new ObjectNotFoundException(format("The movie with id %d wasn't found", film.getId()));
            }
        }
        if (!mpaDao.isContains(film.getMpa().getId())) {
            throw new ObjectNotFoundException(format("MPA for the the movie with id %d wasn't found", film.getId()));
        }
        for (Genre genre : film.getGenres()) {
            if (!genreDao.isContains(genre.getId())) {
                throw new ObjectNotFoundException("Unable to find genre for the movie with id " + film.getId());
            }
        }
    }

    private void checkIfExists(Film film) {
        if (film.getId() != null) {
            if (filmStorage.isContains(film.getId())) {
                throw new ObjectAlreadyExistsException(format("The movie with id %d already exists", film.getId()));
            } else {
                throw new IllegalArgumentException("Unable to set id manually");
            }
        }
        if (!mpaDao.isContains(film.getMpa().getId())) {
            throw new ObjectNotFoundException(format("MPA for the the movie with id %d wasn't found", film.getId()));
        }
        for (Genre genre : film.getGenres()) {
            if (!genreDao.isContains(genre.getId())) {
                throw new ObjectNotFoundException("Unable to find genre for the movie with id " + film.getId());
            }
        }
    }

    private void validate(Film film) {
        log.debug("validate({})", film);
        if (film.getReleaseDate() == null ||
                film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Incorrect release date");
        }
        if (film.getName().isEmpty() || film.getName().isBlank()) {
            throw new ValidationException("Attempt to set an empty movie name");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Attempt to set duration less than zero");
        }
        if (film.getDescription().length() > 200 || film.getDescription().length() == 0) {
            throw new ValidationException("Description increases 200 symbols or empty");
        }
    }

    private void likeChecker(Long filmId, Long userId) {
        log.debug("likeChecker({}, {})", filmId, userId);
        if (!filmStorage.isContains(filmId)) {
            throw new ObjectNotFoundException(format("Unable to find a movie with id %d", filmId));
        }
        if (!userStorage.isContains(userId)) {
            throw new ObjectNotFoundException(format("Unable to find a user with id %d", userId));
        }
    }
}

