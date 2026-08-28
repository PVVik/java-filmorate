package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new ConcurrentHashMap<>();

    @Override
    public Film addFilm(Film film) {
        validNotNull(film);
        validDate(film);
        film.setId(getId());
        film.setLikes(new HashSet<>());
        films.put(film.getId(), film);
        log.info("Создали и добавили фильм с id {}", film.getId());

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!isExists(film)) {
            log.warn("Фильм с переданным id {} не найден при обновлении", film.getId());
            throw new NotFoundException("Фильма с переданным id не существует");
        }
        Film oldFilm = films.get(film.getId());

        if (StringUtils.hasText(film.getName())) {
            oldFilm.setName(film.getName());
            log.info("Обновили название фильма с id {}", film.getId());
        }
        if (StringUtils.hasText(film.getDescription())) {
            oldFilm.setDescription(film.getDescription());
            log.info("Обновили описание фильма с id {}", film.getId());
        }
        if (film.getDuration() != null) {
            oldFilm.setDuration(film.getDuration());
            log.info("Обновили длительность фильма с id {}", film.getId());
        }
        if (film.getReleaseDate() != null) {
            validDate(film);
            oldFilm.setReleaseDate(film.getReleaseDate());
            log.info("Обновили дату выпуска фильма с id {}", film.getId());
        }
        if (film.getLikes() != null) {
            oldFilm.setLikes(film.getLikes());
        }

        films.put(oldFilm.getId(), oldFilm);
        log.info("Завершили обновление фильма с id {}", film.getId());

        return oldFilm;
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(long filmId) {
        if (!films.containsKey(filmId)) {
            log.warn("Запросили несуществующий фильм с id {}", filmId);
            throw new NotFoundException(String.format("Фильма с id %d не существует", filmId));
        }
        log.info("Вернули фильм с id {}", filmId);

        return films.get(filmId);
    }

    private long getId() {
        long maxId = films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++maxId;
    }

    private void validDate(Film film) {
        if (film.getReleaseDate().isBefore(MIN_DATE_FOR_FILM)) {
            log.warn("Ошибка валидации даты для фильма {}", film.getName());
            throw new ValidationException("Дата релиза должна быть позднее 28 декабря 1895 года");
        }
    }

    private void validNotNull(Film film) {
        if (film.getName() == null) {
            log.warn("Не передано имя при создании фильма");
            throw new ValidationException("При создании фильма имя не может быть пустым");
        }
        if (film.getDescription() == null) {
            log.warn("Не передано описание при создании фильма");
            throw new ValidationException("При создании фильма описание не может быть пустым");
        }
        if (film.getReleaseDate() == null) {
            log.warn("Не передана дата релиза при создании фильма");
            throw new ValidationException("При создании фильма дата релиза не может быть пустой");
        }
        if (film.getDuration() == null) {
            log.warn("Не передана продолжительность при создании фильма");
            throw new ValidationException("При создании фильма продолжительность не может быть пустой");
        }
    }

    private boolean isExists(Film film) {
        return films.containsKey(film.getId());
    }
}
