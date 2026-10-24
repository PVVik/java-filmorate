package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public DirectorDto create(DirectorDto directorDto) {
        var director = directorStorage.create(DirectorMapper.mapToEntity(directorDto));

        log.info("Создали режиссера с id {}", director.getId());

        return DirectorMapper.mapToDto(director);
    }

    public DirectorDto update(DirectorDto directorDto) {
        var oldDirector = directorStorage.getById(directorDto.getId());
        var updatedDirector = DirectorMapper.mapToUpdateEntity(oldDirector, directorDto);

        log.info("Обновили режиссера с id {}", updatedDirector.getId());

        return DirectorMapper.mapToDto(directorStorage.update(updatedDirector));
    }

    public List<DirectorDto> getAll() {
        log.info("Вернули список режиссеров");

        return directorStorage.getAll().stream()
                .sorted(Director::compareTo)
                .map(DirectorMapper::mapToDto)
                .toList();
    }

    public DirectorDto getById(long id) {
        var director = directorStorage.getById(id);

        log.info("Вернули режиссера по id {}", director.getId());

        return DirectorMapper.mapToDto(director);
    }

    public List<DirectorDto> getDirectorsByFilmId(long filmId) {
        log.info("Вернули режиссеров фильма с id {}", filmId);

        return directorStorage.getDirectorsByFilmId(filmId).stream()
                .map(DirectorMapper::mapToDto)
                .toList();
    }

    public void delete(long id) {
        var director = directorStorage.getById(id);

        log.info("Удалили режиссера по id {}", director.getId());

        directorStorage.delete(director.getId());
    }

    public void checkDirectorExists(List<DirectorDto> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }

        var requestedIds = directors.stream()
                .map(DirectorDto::getId)
                .filter(Objects::nonNull)
                .toList();

        var existingIds = directorStorage.getExistingIds(requestedIds);

        var missingIds = requestedIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new NotFoundException(String.format("Директора с id %d не существует", missingIds.getFirst()));
        }
    }

    public List<Director> addFilmDirectors(long filmId, List<DirectorDto> directors) {
        var directorIds = directors.stream()
                .map(DirectorDto::getId)
                .collect(Collectors.toCollection(HashSet::new));

        log.info("Добавили режиссеров к фильму с id {}", filmId);

        return directorStorage.addFilmDirectors(filmId, directorIds).stream()
                .map(id -> DirectorMapper.mapToModel(new DirectorDto(id)))
                .toList();
    }

    public void removeFilmDirectors(long filmId) {
        log.info("Удалили режиссеров фильма {}", filmId);

        directorStorage.removeFilmDirectors(filmId);
    }
}
