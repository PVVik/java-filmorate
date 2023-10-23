package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmDbService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmDbService filmService;

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularMovies(@RequestParam(defaultValue = "10") Integer count) {
        return filmService.getPopularMovies(count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeAMovie(@PathVariable Long id, @PathVariable Long userId) {
        filmService.like(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.dislike(id, userId);
    }
}