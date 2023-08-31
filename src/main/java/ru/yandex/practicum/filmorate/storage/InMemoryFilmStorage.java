package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private Map<Integer, Film> films;
    public static final int MAX_FILM_DESCRIPTION_LENGTH = 200;
    public static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, Month.DECEMBER, 28);
    private Integer id;

    public InMemoryFilmStorage() {
        films = new ConcurrentHashMap<>();
        id = 0;
    }

    @Override
    public Film addFilm(Film film) throws ValidationException {
        filmValidate(film);
        films.put(film.getId(), film);
        log.info("Фильм '{}' был сохранён под id '{}'", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) throws ValidationException {
        if (films.containsKey(film.getId())) {
            filmValidate(film);
            films.put(film.getId(), film);
            log.info("Фильм под id '{}' обновлён", film.getId());
            return film;
        } else {
            throw new ObjectNotFoundException("Фильма с id " + film.getId() + " не существует.");
        }
    }

    @Override
    public List<Film> getAllFilms() {
        log.info("Общее количество фильмов: '{}'", films.size());
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(int id) {
        if (!films.containsKey(id)) {
            throw new ObjectNotFoundException("Фильма " + id + " не существует.");
        }
        return films.get(id);
    }

    @Override
    public void deleteFilms() {
        films.clear();
        log.info("Все фильмы удалены.");
    }

    private void filmValidate(Film film) throws ValidationException {
        if (film.getReleaseDate() == null ||
                film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Введена неверная дата релиза.");
        }
        if (film.getName().isEmpty() || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым.");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть больше нуля.");
        }
        if (film.getDescription().length() > MAX_FILM_DESCRIPTION_LENGTH || film.getDescription().length() == 0) {
            throw new ValidationException("Неверное количество символов в описании.");
        }
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        if (film.getId() <= 0) {
            film.setId(++id);
        }
    }
}
