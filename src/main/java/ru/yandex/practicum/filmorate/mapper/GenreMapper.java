package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

public class GenreMapper {

    public static GenreDto mapToDto(Genre genre) {
        return new GenreDto(genre.getId(), genre.getName());
    }
}
