package ru.yandex.practicum.filmorate.mapper;

import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

public class UserMapper {

    public static User mapToUser(UserDto userDto) {
        return User.builder().email(userDto.getEmail()).login(userDto.getLogin()).name(userDto.getName())
                .birthday(userDto.getBirthday()).build();
    }

    public static UserDto mapToDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
    }

    public static User mapToUpdateUser(User user, UserDto userDto) {
        if (StringUtils.hasText(userDto.getEmail())) {
            user.setEmail(userDto.getEmail());
        }
        if (StringUtils.hasText(userDto.getLogin())) {
            user.setLogin(userDto.getLogin());
        }
        if (userDto.getBirthday() != null) {
            user.setBirthday(userDto.getBirthday());
        }
        if (StringUtils.hasText(userDto.getName())) {
            user.setName(userDto.getName());
        }

        return user;
    }
}
