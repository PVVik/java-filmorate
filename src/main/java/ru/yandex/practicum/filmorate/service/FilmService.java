package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
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
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Operation;
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
    private final EventService eventService;

    public FilmService(
            @Qualifier("FilmDbStorage") FilmStorage filmStorage,
            UserService userService,
            MpaService mpaService,
            GenreService genreService,
            DirectorService directorService,
            EventService eventService
    ) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.directorService = directorService;
        this.eventService = eventService;
    }

    @Transactional
    public void addLike(long filmId, long userId) {
        checkFilmExists(filmId);
        userService.checkUserExists(userId);

        filmStorage.addLike(filmId, userId);
        eventService.addLikeEvent(userId, filmId, Operation.ADD);
        log.info("Добавили лайк пользователя с id {} к фильму с id {}", userId, filmId);
    }

    @Transactional
    public void deleteLike(long filmId, long userId) {
        checkFilmExists(filmId);
        userService.checkUserExists(userId);

        filmStorage.deleteLike(filmId, userId);
        eventService.addLikeEvent(userId, filmId, Operation.REMOVE);
        log.info("Удалили лайк пользователя с id {} с фильма с id {}", userId, filmId);
    }

    public List<FilmDto> getPopularFilms(long count, Long genreId, Integer year) {
        var films = filmStorage.getPopularFilms(count, genreId, year);

        log.info("Обработали запрос на получение списка популярных фильмов, найдено фильмов: {}", films.size());

        return films.stream()
                .map(film -> {
                    var dto = FilmMapper.mapToDto(film);
                    dto.setGenres(genreService.getGenresByFilmId(film.getId()));
                    if (film.getMpaId() != null) {
                        dto.setMpa(mpaService.getMpaById(film.getMpaId()));
                    }
                    dto.setDirectors(directorService.getDirectorsByFilmId(film.getId()));
                    return dto;
                })
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

    @Transactional
    public FilmDto addFilm(FilmDto filmDto) {
        checkValidDate(filmDto);
        mpaService.checkMpaExists(filmDto.getMpa().getId());

        if (filmDto.getGenres() != null && !filmDto.getGenres().isEmpty()) {
            var uniqueGenres = filmDto.getGenres().stream()
                    .distinct()
                    .collect(Collectors.toList());
            genreService.checkGenresExists(uniqueGenres);
        }

        if (filmDto.getDirectors() != null && !filmDto.getDirectors().isEmpty()) {
            var uniqueDirectors = filmDto.getDirectors().stream()
                    .distinct()
                    .collect(Collectors.toList());
            directorService.checkDirectorExists(uniqueDirectors);
        }

        var film = filmStorage.addFilm(FilmMapper.mapToFilm(filmDto));

        if (filmDto.getGenres() != null && !filmDto.getGenres().isEmpty()) {
            var uniqueGenres = filmDto.getGenres().stream()
                    .distinct()
                    .collect(Collectors.toList());

            var genreIds = uniqueGenres.stream()
                    .map(GenreDto::getId)
                    .toList();

            genreService.addFilmGenres(film.getId(), genreIds);

            var genresObjects = uniqueGenres.stream()
                    .map(GenreMapper::mapToModel)
                    .collect(Collectors.toList());
            film.setGenres(genresObjects);
        } else {
            film.setGenres(List.of());
        }

        if (filmDto.getDirectors() != null && !filmDto.getDirectors().isEmpty()) {
            var uniqueDirectors = filmDto.getDirectors().stream()
                    .distinct()
                    .collect(Collectors.toList());

            var directorsObjects = directorService.addFilmDirectors(film.getId(), uniqueDirectors);
            film.setDirectors(directorsObjects);
        } else {
            film.setDirectors(List.of());
        }

        log.info("Добавили фильм с id {}", film.getId());
        return FilmMapper.mapToDto(film);
    }

    @Transactional
    public FilmDto updateFilm(FilmDto filmDto) {
        var film = filmStorage.getFilmById(filmDto.getId());
        var updateFilm = FilmMapper.mapToUpdateFilm(film, filmDto);

        if (filmDto.getMpa() != null) {
            mpaService.checkMpaExists(filmDto.getMpa().getId());
            updateFilm.setMpaId(filmDto.getMpa().getId());
        }

        if (filmDto.getDirectors() == null || filmDto.getDirectors().isEmpty()) {
            directorService.removeFilmDirectors(updateFilm.getId());
            updateFilm.setDirectors(List.of());
        } else {
            var uniqueDirectors = filmDto.getDirectors().stream()
                    .distinct()
                    .collect(Collectors.toList());

            directorService.checkDirectorExists(uniqueDirectors);
            directorService.removeFilmDirectors(updateFilm.getId());

            var directorsObjects = directorService.addFilmDirectors(updateFilm.getId(), uniqueDirectors);
            updateFilm.setDirectors(directorsObjects);
        }

        if (filmDto.getGenres() == null || filmDto.getGenres().isEmpty()) {
            genreService.removeFilmGenres(updateFilm.getId());
            updateFilm.setGenres(List.of());
        } else {
            var uniqueGenres = filmDto.getGenres().stream()
                    .distinct()
                    .collect(Collectors.toList());

            genreService.checkGenresExists(uniqueGenres);
            genreService.removeFilmGenres(updateFilm.getId());

            var genreIds = uniqueGenres.stream()
                    .map(GenreDto::getId)
                    .toList();

            genreService.addFilmGenres(updateFilm.getId(), genreIds);

            var genresObjects = uniqueGenres.stream()
                    .map(GenreMapper::mapToModel)
                    .collect(Collectors.toList());
            updateFilm.setGenres(genresObjects);
        }

        log.info("Обновили фильм с id {}", updateFilm.getId());

        return getFilmDtoWithGenresAndMpaAndDirectors(
                filmStorage.updateFilm(updateFilm),
                getGenreMap(),
                getMpaMap(),
                getDirectorMap()
        );
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
        var genreMap = getGenreMap();
        var mpaMap = getMpaMap();
        var directorMap = getDirectorMap();

        String field = null;
        String order = "asc";

        if (StringUtils.hasText(sortBy)) {
            String[] stringsSortBy = sortBy.split(",");
            checkSortParameters(stringsSortBy);

            if (stringsSortBy.length == 2) {
                field = stringsSortBy[0];
                order = stringsSortBy[1];
            } else if (stringsSortBy.length == 1) {
                field = stringsSortBy[0];
            }
        }


        return filmStorage.getByDirectorId(director.getId(), field, order).stream()
                .map(film -> getFilmDtoWithGenresAndMpaAndDirectors(film, genreMap, mpaMap, directorMap))
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

    public List<FilmDto> search(String query, String by) {
        String[] sortBy = by.split(",");
        List<Film> findFilms = filmStorage.search(query, List.of(sortBy));
        var genreMap = getGenreMap();
        var mpaMap = getMpaMap();
        var directorMap = getDirectorMap();

        return findFilms.stream()
                .map(film -> getFilmDtoWithGenresAndMpaAndDirectors(film, genreMap, mpaMap, directorMap))
                .toList();
    }

    public List<FilmDto> getRecommendations(long userId) {
        log.info("Получение рекомендаций для пользователя {}", userId);

        userService.checkUserExists(userId);

        Set<Long> userLiked = filmStorage.findLikedFilmIdsByUser(userId);

        if (userLiked.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<Long> bestUserId = filmStorage.findMostSimilarUserId(userId);
        if (bestUserId.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> similarLiked = filmStorage.findLikedFilmIdsByUser(bestUserId.get());
        similarLiked.removeAll(userLiked);

        return similarLiked.stream()
                .map(this::getFilmById)
                .collect(Collectors.toList());
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

    private FilmDto getFilmDtoWithGenresAndMpaAndDirectors(
            Film film,
            Map<Long, GenreDto> genreMap,
            Map<Long, MpaDto> mpaMap,
            Map<Long, DirectorDto> directorMap
    ) {
        var dto = FilmMapper.mapToDto(film);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            var filmGenres = film.getGenres().stream()
                    .map(genre -> genreMap.get(genre.getId()))
                    .filter(Objects::nonNull)
                    .toList();
            dto.setGenres(filmGenres);
        } else {
            dto.setGenres(List.of());
        }

        if (film.getMpaId() != null) {
            dto.setMpa(mpaMap.get(film.getMpaId()));
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            var filmDirectors = film.getDirectors().stream()
                    .map(director -> directorMap.get(director.getId()))
                    .filter(Objects::nonNull)
                    .toList();
            dto.setDirectors(filmDirectors);
        } else {
            dto.setDirectors(List.of());
        }

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