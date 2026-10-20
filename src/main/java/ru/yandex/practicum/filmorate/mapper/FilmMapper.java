package ru.yandex.practicum.filmorate.mapper;

import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashSet;
import java.util.Set;

public class FilmMapper {

    public static FilmDto mapToDto(Film film) {
        MpaDto mpaDto;
        Set<GenreDto> genreDtos = new HashSet<>();
        Set<DirectorDto> directorDtos = new HashSet<>();

        if (film.getMpaId() != null) {
            mpaDto = new MpaDto(film.getMpaId());
        } else mpaDto = null;

        if (film.getGenres() != null) {
            for (Long gId : film.getGenres()) {
                genreDtos.add(new GenreDto(gId));
            }
        }

        if (film.getDirectors() != null) {
            for (Long dId : film.getDirectors()) {
                directorDtos.add(new DirectorDto(dId));
            }
        }

        return new FilmDto(film.getId(), film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                mpaDto, genreDtos, directorDtos);
    }

    public static Film mapToFilm(FilmDto filmDto) {
        Long mpaId;
        Set<Long> genres = new HashSet<>();
        Set<Long> directors = new HashSet<>();

        if (filmDto.getMpa() != null) {
            mpaId = filmDto.getMpa().getId();
        } else mpaId = null;

        if (filmDto.getGenres() != null) {
            for (GenreDto g : filmDto.getGenres()) {
                genres.add(g.getId());
            }
        }

        if (filmDto.getDirectors() != null) {
            for (DirectorDto d : filmDto.getDirectors()) {
                directors.add(d.getId());
            }
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
