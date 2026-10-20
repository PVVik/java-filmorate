package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
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
    private final DirectorService directorService;

    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage, UserService userService,
                       MpaService mpaService, GenreService genreService, DirectorService directorService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.directorService = directorService;
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
        var genreMap = getGenreMap();
        var mpaMap = getMpaMap();
        var directorMap = getDirectorMap();

        log.info("Обработали запрос на получение списка популярных фильмов");

        return films.stream()
                .map(film -> getFilmDtoWithGenresAndMpaAndDirectors(film, genreMap, mpaMap, directorMap))
                .toList();
    }

    public List<FilmDto> getCommonFilms(long userId, long friendId) {
        userService.checkUserExists(userId);
        userService.checkUserExists(friendId);

        var genreMap = getGenreMap();
        var mpaMap = getMpaMap();
        var directorMap = getDirectorMap();

        var commonFilms = filmStorage.getCommonFilms(userId, friendId);
        if (commonFilms == null) {
            commonFilms = List.of();
        }

        log.info("Обработали запрос на общие фильмы: userId={}, friendId={}. Найдено фильмов: {}",
                userId, friendId, commonFilms.size());

        return commonFilms.stream()
                .map(film -> getFilmDtoWithGenresAndMpaAndDirectors(film, genreMap, mpaMap, directorMap))
                .toList();
    }

    public FilmDto addFilm(FilmDto filmDto) {
        checkValidDate(filmDto);
        mpaService.checkMpaExists(filmDto.getMpa().getId());

        if (filmDto.getGenres() != null) {
            genreService.checkGenresExists(filmDto.getGenres());
        }

        if (filmDto.getDirectors() != null) {
            directorService.checkDirectorExists(filmDto.getDirectors());
        }

        var film = filmStorage.addFilm(FilmMapper.mapToFilm(filmDto));

        if (filmDto.getGenres() != null && !filmDto.getGenres().isEmpty()) {
            var filmGenres = genreService.addFilmGenres(film.getId(), filmDto.getGenres());
            film.setGenres(filmGenres);
        } else {
            film.setGenres(new HashSet<>());
        }

        if (filmDto.getDirectors() != null && !filmDto.getDirectors().isEmpty()) {
            var filmDirectors = directorService.addFilmDirectors(film.getId(), filmDto.getDirectors());
            film.setDirectors(filmDirectors);
        } else {
            film.setDirectors(new HashSet<>());
        }

        log.info("Добавили фильм с id {}", film.getId());

        return FilmMapper.mapToDto(film);
    }

    public FilmDto updateFilm(FilmDto filmDto) {
        var film = filmStorage.getFilmById(filmDto.getId());
        var updateFilm = FilmMapper.mapToUpdateFilm(film, filmDto);

        if (filmDto.getDirectors() != null) {
            directorService.checkDirectorExists(filmDto.getDirectors());
            directorService.removeFilmDirectors(updateFilm.getId()); // удаляем старые связи

            if (!filmDto.getDirectors().isEmpty()) {
                var newDirectors = directorService.addFilmDirectors(updateFilm.getId(), filmDto.getDirectors());
                updateFilm.setDirectors(newDirectors);
            } else {
                updateFilm.setDirectors(new HashSet<>());
            }
        }

        log.info("Обновили фильм с id {}", updateFilm.getId());

        return FilmMapper.mapToDto(filmStorage.updateFilm(film));
    }

    public List<FilmDto> getFilms() {
        var genreMap = getGenreMap();
        var mpaMap = getMpaMap();
        var directorMap = getDirectorMap();

        log.info("Обработали запрос на получение всех фильмов");

        return filmStorage.getFilms().stream()
                .map(film -> getFilmDtoWithGenresAndMpaAndDirectors(film, genreMap, mpaMap, directorMap))
                .toList();
    }

    public FilmDto getFilmById(long filmId) {
        var film = filmStorage.getFilmById(filmId);
        var genreMap = getGenreMap();
        var mpaMap = getMpaMap();
        var directorMap = getDirectorMap();

        log.info("Получили фильм по id {}", filmId);

        return getFilmDtoWithGenresAndMpaAndDirectors(film, genreMap, mpaMap, directorMap);
    }

    public List<FilmDto> getFilmsByDirectorId(long directorId, String sortBy) {
        var director = directorService.getById(directorId);

        String[] stringsSortBy;
        if (StringUtils.hasText(sortBy)) {
            stringsSortBy = sortBy.split(",");
            checkSortParameters(stringsSortBy);

            if (stringsSortBy.length == 2) {
                return filmStorage.getByDirectorId(director.getId()).stream()
                        .sorted(Comparator.comparing(f -> f.getReleaseDate().getYear()))
                        .sorted(Comparator.comparing(f -> f.getLikes().size()))
                        .map(FilmMapper::mapToDto)
                        .toList();
            } else if (stringsSortBy.length == 1 && stringsSortBy[0].equals("year")) {
                return filmStorage.getByDirectorId(director.getId()).stream()
                        .sorted(Comparator.comparing(f -> f.getReleaseDate().getYear()))
                        .map(FilmMapper::mapToDto)
                        .toList();
            } else if (stringsSortBy.length == 1 && stringsSortBy[0].equals("likes")) {
                filmStorage.getByDirectorId(director.getId()).stream()
                        .sorted(Comparator.comparing(f -> f.getLikes().size()))
                        .map(FilmMapper::mapToDto)
                        .toList();
            }
        }
        return filmStorage.getByDirectorId(director.getId()).stream()
                .map(FilmMapper::mapToDto)
                .toList();
    }

    public void checkFilmExists(long filmId) {
        filmStorage.getFilmById(filmId);
    }

    public void deleteFilm(long filmId) {
        checkFilmExists(filmId);
        filmStorage.deleteFilm(filmId);
        log.info("Удалили фильм с id {}", filmId);
    }

    private Map<Long, GenreDto> getGenreMap() {
        return genreService.getGenres().stream()
                .collect(Collectors.toMap(GenreDto::getId, Function.identity()));
    }

    private Map<Long, MpaDto> getMpaMap() {
        return mpaService.getMpas().stream()
                .collect(Collectors.toMap(MpaDto::getId, Function.identity()));
    }

    private Map<Long, DirectorDto> getDirectorMap() {
        return directorService.getAll().stream()
                .collect(Collectors.toMap(DirectorDto::getId, Function.identity()));
    }

    private void checkValidDate(FilmDto filmDto) {
        if (filmDto.getReleaseDate().isBefore(FilmStorage.MIN_DATE_FOR_FILM)) {
            log.warn("Получили исключение при вводе неверной даты релиза {}", filmDto.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть позднее 28 декабря 1895 года");
        }
    }

    private FilmDto getFilmDtoWithGenresAndMpaAndDirectors(Film film, Map<Long, GenreDto> genreMap,
                                                           Map<Long, MpaDto> mpaMap, Map<Long, DirectorDto> directorMap) {
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

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            var filmDirectors = film.getDirectors().stream()
                    .sorted()
                    .map(directorMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            dto.setDirectors(filmDirectors);
        } else dto.setDirectors(new LinkedHashSet<>());

        return dto;
    }

    private void checkSortParameters(String[] sortBy) {
        List<String> s = Arrays.stream(sortBy)
                .filter(string -> string.equals("year") || string.equals("likes"))
                .toList();

        if (s.isEmpty()) {
            log.warn("Поступил запрос с неподдерживаемыми параметрами сортировки: {}", (Object) sortBy);
            throw new ValidationException(String.format("Неверные параметры запроса: %s", (Object) sortBy));
        }
    }

}
