package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        checkFilmExists(filmId);
        userService.checkUserExists(userId);

        filmStorage.addLike(filmId, userId);
        log.info("Добавили лайк пользователя с id {} к фильму с id {}", userId, filmId);
    }

    public void deleteLike(long filmId, long userId) {
        checkFilmExists(filmId);
        userService.checkUserExists(userId);

        filmStorage.deleteLike(filmId, userId);
        log.info("Удалили лайк пользователя с id {} с фильма с id {}", userId, filmId);
    }

    public List<FilmDto> getPopularFilms(long count) {
        var films = filmStorage.getPopularFilms(count);
        var genreMap = genreService.getGenres().stream()
                .collect(Collectors.toMap(GenreDto::getId, Function.identity()));
        var mpaMap = mpaService.getMpas().stream()
                .collect(Collectors.toMap(MpaDto::getId, Function.identity()));

        log.info("Обработали запрос на получение списка популярных фильмов");

        return films.stream()
                .map(film -> getFilmDtoWithGenresAndMpa(film, genreMap, mpaMap))
                .toList();
    }

    public FilmDto addFilm(FilmDto filmDto) {
        checkValidDate(filmDto);
        mpaService.checkMpaExists(filmDto.getMpa().getId());

        if (filmDto.getGenres() != null) {
            genreService.checkGenresExists(filmDto.getGenres());
        }

        var film = filmStorage.addFilm(FilmMapper.mapToFilm(filmDto));

        if (filmDto.getGenres() != null && !filmDto.getGenres().isEmpty()) {
            var filmGenres = genreService.addFilmGenres(film.getId(), filmDto.getGenres());
            film.setGenres(filmGenres);
        } else {
            film.setGenres(new HashSet<>());
        }

        log.info("Добавили фильм с id {}", film.getId());

        return FilmMapper.mapToDto(film);
    }

    public FilmDto updateFilm(FilmDto filmDto) {
        var film = filmStorage.getFilmById(filmDto.getId());
        var updateFilm = FilmMapper.mapToUpdateFilm(film, filmDto);

        log.info("Обновили фильм с id {}", updateFilm.getId());

        return FilmMapper.mapToDto(filmStorage.updateFilm(film));
    }

    public List<FilmDto> getFilms() {
        var genreMap = genreService.getGenres().stream()
                .collect(Collectors.toMap(GenreDto::getId, Function.identity()));
        var mpaMap = mpaService.getMpas().stream()
                .collect(Collectors.toMap(MpaDto::getId, Function.identity()));

        log.info("Обработали запрос на получение всех фильмов");

        return filmStorage.getFilms().stream()
                .map(film -> getFilmDtoWithGenresAndMpa(film, genreMap, mpaMap))
                .toList();
    }

    public FilmDto getFilmById(long filmId) {
        var film = filmStorage.getFilmById(filmId);
        var genreMap = genreService.getGenres().stream()
                .collect(Collectors.toMap(GenreDto::getId, Function.identity()));
        var mpaMap = mpaService.getMpas().stream()
                .collect(Collectors.toMap(MpaDto::getId, Function.identity()));

        log.info("Получили фильм по id {}", filmId);

        return getFilmDtoWithGenresAndMpa(film, genreMap, mpaMap);
    }

    private void checkValidDate(FilmDto filmDto) {
        if (filmDto.getReleaseDate().isBefore(FilmStorage.MIN_DATE_FOR_FILM)) {
            log.warn("Получили исключение при вводе неверной даты релиза {}", filmDto.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть позднее 28 декабря 1895 года");
        }
    }

    private void checkFilmExists(long filmId) {
        filmStorage.getFilmById(filmId);
    }

    private FilmDto getFilmDtoWithGenresAndMpa(Film film, Map<Long, GenreDto> genreMap, Map<Long, MpaDto> mpaMap) {
        var dto = FilmMapper.mapToDto(film);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            var filmGenres = film.getGenres().stream()
                    .sorted()
                    .map(genreMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            dto.setGenres(filmGenres);
        } else {
            dto.setGenres(new LinkedHashSet<>());
        }

        if (film.getMpaId() != null) {
            dto.setMpa(mpaMap.get(film.getMpaId()));
        }

        return dto;
    }

}
