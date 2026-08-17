package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    public static final LocalDate MIN_DATE_FOR_FILM = LocalDate.of(1895, Month.DECEMBER, 28);
    private final Map<Long, Film> films = new ConcurrentHashMap<>();

    @PostMapping
    public Film addFilm(@RequestBody @Valid Film film) {
        validNotNull(film);
        validDate(film);
        film.setId(getId());
        films.put(film.getId(), film);
        log.info("Создали и добавили фильм с id {}", film.getId());

        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film film) {
        if (!isExists(film)) {
            log.warn("Фильм с переданным id {} не найден при обновлении", film.getId());
            throw new NoSuchElementException("Фильма с переданным id не существует");
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
        films.put(oldFilm.getId(), oldFilm);
        log.info("Завершили обновление фильма с id {}", film.getId());

        return oldFilm;
    }

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
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
