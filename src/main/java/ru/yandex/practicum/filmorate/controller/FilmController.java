package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public FilmDto addFilm(@RequestBody @Valid FilmDto filmDto) {
        return filmService.addFilm(filmDto);
    }

    @PutMapping
    public FilmDto updateFilm(@RequestBody @Valid FilmDto film) {
        return filmService.updateFilm(film);
    }

    @GetMapping
    public List<FilmDto> getFilms() {
        return filmService.getFilms();
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable long filmId, @PathVariable long userId) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void deleteLike(@PathVariable long filmId, @PathVariable long userId) {
        filmService.deleteLike(filmId, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(
            @RequestParam(defaultValue = "10") String count,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer year) {
        return filmService.getPopularFilms(
                Integer.parseInt(count),
                genreId,
                year
        );
    }

    @GetMapping("/common")
    public List<FilmDto> getCommonFilms(@RequestParam long userId, @RequestParam long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/{filmId}")
    public FilmDto getFilmById(@PathVariable long filmId) {
        return filmService.getFilmById(filmId);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable long filmId) {
        filmService.deleteFilm(filmId);
    }

    @GetMapping("/director/{directorId}")
    public List<FilmDto> getFilmsByDirector(@PathVariable Long directorId,
                                            @RequestParam(required = false) String sortBy) {
        return filmService.getFilmsByDirectorId(directorId, sortBy);
    }

}
