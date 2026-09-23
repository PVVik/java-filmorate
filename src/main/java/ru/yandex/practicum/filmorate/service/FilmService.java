package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final MpaService mpaService;
    private final GenreService genreService;

    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage, UserService userService, MpaService mpaService, GenreService genreService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public void addLike(long filmId, long userId) {
        filmStorage.getFilmById(filmId);
        userService.getUserById(userId);

        filmStorage.addLike(filmId, userId);
        log.info("Добавили лайк пользователя с id {} к фильму с id {}", userId, filmId);
    }

    public void deleteLike(long filmId, long userId) {
        filmStorage.getFilmById(filmId);
        userService.getUserById(userId);

        filmStorage.deleteLike(filmId, userId);
        log.info("Удалили лайк пользователя с id {} с фильма с id {}", userId, filmId);
    }

    public List<FilmDto> getPopularFilms(long count) {
        List<Film> films = filmStorage.getPopularFilms(count);

        log.info("Обработали запрос на получение списка популярных фильмов");

        return films.stream().map(FilmMapper::mapToDto)
                .peek(filmDto -> filmDto.setGenres(genreService.getGenresByFilmId(filmDto.getId())))
                .peek(filmDto -> filmDto.setMpa(mpaService.getMpaById(filmDto.getMpa().getId())))
                .toList();
    }

    public FilmDto addFilm(FilmDto filmDto) {
        checkValidDate(filmDto);
        mpaService.getMpaById(filmDto.getMpa().getId());

        if (filmDto.getGenres() != null) {
            for (GenreDto genre : filmDto.getGenres()) {
                genreService.getGenreById(genre.getId());
            }
        }

        Film film = filmStorage.addFilm(FilmMapper.mapToFilm(filmDto));

        if (filmDto.getGenres() != null && !filmDto.getGenres().isEmpty()) {
            Set<Long> filmGenres = genreService.addFilmGenres(film.getId(), filmDto.getGenres());
            film.setGenres(filmGenres);
        } else {
            film.setGenres(new HashSet<>());
        }

        log.info("Добавили фильм с id {}", film.getId());

        return FilmMapper.mapToDto(film);
    }

    public FilmDto updateFilm(FilmDto filmDto) {
        Film film = filmStorage.getFilmById(filmDto.getId());
        Film updateFilm = FilmMapper.mapToUpdateFilm(film, filmDto);

        log.info("Обновили фильм с id {}", updateFilm.getId());

        return FilmMapper.mapToDto(filmStorage.updateFilm(film));
    }

    public List<FilmDto> getFilms() {
        log.info("Обработали запрос на получение всех фильмов");

        return filmStorage.getFilms().stream()
                .map(FilmMapper::mapToDto)
                .peek(filmDto -> filmDto.setGenres(genreService.getGenresByFilmId(filmDto.getId())))
                .peek(filmDto -> filmDto.setMpa(mpaService.getMpaById(filmDto.getMpa().getId())))
                .toList();
    }

    public FilmDto getFilmById(long filmId) {
        Film film = filmStorage.getFilmById(filmId);
        FilmDto filmDto = FilmMapper.mapToDto(film);

        filmDto.setGenres(genreService.getGenresByFilmId(filmId));
        filmDto.setMpa(mpaService.getMpaById(film.getMpaId()));

        log.info("Получили фильм по id {}", filmId);

        return filmDto;
    }

    private void checkValidDate(FilmDto filmDto) {
        if (filmDto.getReleaseDate().isBefore(FilmStorage.MIN_DATE_FOR_FILM)) {
            log.warn("Получили исключение при вводе неверной даты релиза {}", filmDto.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть позднее 28 декабря 1895 года");
        }
    }

}
