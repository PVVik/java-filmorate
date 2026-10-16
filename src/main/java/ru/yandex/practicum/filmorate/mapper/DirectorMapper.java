package ru.yandex.practicum.filmorate.mapper;

import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;

public class DirectorMapper {

    public static Director mapToEntity(DirectorDto directorDto) {
        return Director.builder().name(directorDto.getName()).build();
    }

    public static DirectorDto mapToDto(Director director) {
        return new DirectorDto(director.getId(), director.getName());
    }

    public static Director mapToUpdateEntity(Director director, DirectorDto directorDto) {
        if (StringUtils.hasText(directorDto.getName())) {
            director.setName(directorDto.getName());
        }

        return director;
    }
}
