package ru.yandex.practicum.filmorate.mapper;

import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collections;
import java.util.List;

public class FilmMapper {

    public static FilmDto mapToDto(Film film) {
        MpaDto mpaDto;
        List<GenreDto> genreDtos = Collections.emptyList();
        List<DirectorDto> directorDtos = Collections.emptyList();

        if (film.getMpaId() != null) {
            mpaDto = new MpaDto(film.getMpaId());
        } else mpaDto = null;

        if (film.getGenres() != null) {
            genreDtos = film.getGenres().stream()
                    .map(GenreMapper::mapToDto)
                    .toList();
        }

        if (film.getDirectors() != null) {
            directorDtos = film.getDirectors().stream()
                    .map(DirectorMapper::mapToDto)
                    .toList();
        }

        return new FilmDto(film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                mpaDto, genreDtos, directorDtos);
    }

    public static Film mapToFilm(FilmDto filmDto) {
        Long mpaId;
        List<Genre> genres = Collections.emptyList();
        List<Director> directors = Collections.emptyList();

        if (filmDto.getMpa() != null) {
            mpaId = filmDto.getMpa().getId();
        } else mpaId = null;

        if (filmDto.getGenres() != null) {
            genres = filmDto.getGenres().stream()
                    .map(GenreMapper::mapToModel)
                    .toList();
        }

        if (filmDto.getDirectors() != null) {
            directors = filmDto.getDirectors().stream()
                    .map(DirectorMapper::mapToModel)
                    .toList();
        }

        return Film.builder().name(filmDto.getName()).description(filmDto.getDescription())
                .releaseDate(filmDto.getReleaseDate()).duration(filmDto.getDuration())
                .mpaId(mpaId).genres(genres).directors(directors).build();
    }

    public static Film mapToUpdateFilm(Film film, FilmDto filmDto) {
        if (StringUtils.hasText(filmDto.getName())) {
            film.setName(filmDto.getName());
        }
        if (StringUtils.hasText(filmDto.getDescription())) {
            film.setDescription(filmDto.getDescription());
        }
        if (filmDto.getDuration() != null) {
            film.setDuration(filmDto.getDuration());
        }
        if (filmDto.getReleaseDate() != null) {
            film.setReleaseDate(filmDto.getReleaseDate());
        }

        return film;
    }
}