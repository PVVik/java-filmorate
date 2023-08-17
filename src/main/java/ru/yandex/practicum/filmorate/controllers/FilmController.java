package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
public class FilmController {

    private Map<Integer, Film> films = new ConcurrentHashMap<>();
    public static final int MAX_FILM_DESCRIPTION_LENGTH = 200;
    public static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1985, Month.DECEMBER, 28);
    private int id = 0;

    @PostMapping(value = "/films")
    @ResponseBody
    public Film addFilm(@RequestBody Film film) throws ValidationException {
        filmValidate(film);
        films.put(film.getId(), film);
        log.info("Фильм '{}' был сохранён под id '{}'", film.getName(), film.getId());
        return film;
    }

    @PutMapping("/films")
    @ResponseBody
    public Film updateFilm(@RequestBody Film film) throws ValidationException {
        filmValidate(film);
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм под id '{}' обновлён", film.getId());
        } else {
            throw new ValidationException("Фильма с id " + film.getId() + " не существует.");
        }
        return film;
    }

    @GetMapping("/films")
    @ResponseBody
    public List<Film> getAllFilms() {
        log.info("Общее количество фильмов: '{}'", films.size());
        return new ArrayList<>(films.values());
    }

    private void filmValidate(Film film) throws ValidationException {
        if (film.getReleaseDate() == null ||
                film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Введена неверная дата релиза.");
        }
        if (film.getName().isEmpty() || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым.");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть больше нуля.");
        }
        if (film.getDescription().length() > 200 || film.getDescription().length() == 0) {
            throw new ValidationException("Неверное количество символов в описании.");
        }
        if (film.getId() <= 0) {
            film.setId(++id);
        }
    }
}

