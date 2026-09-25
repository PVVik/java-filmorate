package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {

    private final GenreStorage genreStorage;

    public List<GenreDto> getGenres() {
        log.info("Обработали запрос на получение всех жанров");

        return genreStorage.getGenres().stream().map(GenreMapper::mapToDto).toList();
    }

    public GenreDto getGenreById(long genreId) {
        log.info("Получили жанр по id {}", genreId);

        return GenreMapper.mapToDto(genreStorage.getGenreById(genreId));
    }

    public Set<Long> addFilmGenres(long filmId, Set<GenreDto> genres) {
        var genreIds = genres.stream()
                .map(GenreDto::getId)
                .collect(Collectors.toSet());

        log.info("Добавили жанры к фильму с id {}", filmId);

        return genreStorage.addFilmGenres(filmId, genreIds);
    }

    public void checkGenresExists(Set<GenreDto> genres) {
        var allGenres = getGenres();
        var existingIds = allGenres.stream()
                .map(GenreDto::getId)
                .collect(Collectors.toSet());

        var missingIds = genres.stream()
                .map(GenreDto::getId)
                .filter(id -> !existingIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new NotFoundException(String.format("Жанра с id %d не существует", missingIds.getFirst()));
        }

    }
}
