package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private Map<Integer, Film> films;
    private int id;

    public InMemoryFilmStorage() {
        films = new ConcurrentHashMap<>();
        id = 0;
    }

    @Override
    public Film addFilm(Film film) {
        setId(film);
        films.put(film.getId(), film);
        log.info("Фильм '{}' был сохранён под id '{}'", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (films.containsKey(film.getId())) {
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

    @Override
    public List<Film> getPopularFilms(Integer count) {
        log.info("Показаны самые популярные фильмы.");
        return getAllFilms()
                .stream()
                .sorted(Comparator.comparing(f -> f.getLikes().size(), Comparator.reverseOrder()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void setId(Film film) {
        if (film.getId() <= 0) {
            film.setId(++id);
        }
    }
}
