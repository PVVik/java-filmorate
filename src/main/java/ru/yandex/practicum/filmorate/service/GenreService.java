package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {

    private final GenreStorage genreStorage;

    public List<GenreDto> getGenres() {
        log.info("Обработали запрос на получение всех жанров");
        return genreStorage.getGenresDto();
    }

    public GenreDto getGenreById(long genreId) {
        log.info("Получили жанр по id {}", genreId);
        var genre = genreStorage.getGenreById(genreId);
        return GenreMapper.mapToDto(genre);
    }

    public List<GenreDto> getGenresByFilmId(long filmId) {
        log.info("Получили жанры фильма с id {}", filmId);
        return genreStorage.getGenresByFilmId(filmId);
    }

    public List<Long> addFilmGenres(long filmId, List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return List.of();
        }

        genreStorage.removeFilmGenres(filmId);

        return genreStorage.addFilmGenres(filmId, genreIds);
    }

    public void checkGenresExists(List<GenreDto> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        var allGenres = getGenres();
        var existingIds = allGenres.stream()
                .map(GenreDto::getId)
                .toList();

        var missingIds = genres.stream()
                .map(GenreDto::getId)
                .filter(id -> !existingIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new NotFoundException(String.format("Жанра с id %d не существует", missingIds.getFirst()));
        }
    }

    public void removeFilmGenres(long filmId) {
        log.info("Удалили жанры фильма {}", filmId);
        genreStorage.removeFilmGenres(filmId);
    }

}
