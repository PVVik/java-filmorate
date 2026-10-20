package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
class FeedLikeEventIntegrationTest {

    @Autowired
    private FilmService filmService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Test
    @DisplayName("Поставить лайк — в ленте появляется событие LIKE ADD")
    void addLike_shouldCreateFeedEvent() {
        FilmDto film = filmService.addFilm(validFilm());
        UserDto user = userService.addUser(validUser("it-feed1@test.com", "itfeed1"));

        filmService.addLike(film.getId(), user.getId());

        List<EventDto> feed = eventService.getFeed(user.getId());

        assertThat(feed).hasSize(1);
        EventDto event = feed.getFirst();
        assertThat(event.getUserId()).isEqualTo(user.getId());
        assertThat(event.getEventType()).isEqualTo("LIKE");
        assertThat(event.getOperation()).isEqualTo("ADD");
        assertThat(event.getEntityId()).isEqualTo(film.getId());
        assertThat(event.getTimestamp()).isPositive();
    }

    @Test
    @DisplayName("Убрать лайк — в ленте появляются LIKE ADD и LIKE REMOVE, свежий первым")
    void deleteLike_shouldAddRemoveEvent() {
        FilmDto film = filmService.addFilm(validFilm());
        UserDto user = userService.addUser(validUser("it-feed2@test.com", "itfeed2"));

        filmService.addLike(film.getId(), user.getId());
        filmService.deleteLike(film.getId(), user.getId());

        List<EventDto> feed = eventService.getFeed(user.getId());

        assertThat(feed).hasSize(2);
        assertThat(feed.get(0).getOperation()).isEqualTo("REMOVE");
        assertThat(feed.get(1).getOperation()).isEqualTo("ADD");
    }

    @Test
    @DisplayName("Добавить друга — в ленте появляется событие FRIEND ADD")
    void addFriend_shouldCreateFeedEvent() {
        UserDto user1 = userService.addUser(validUser("it-feed3@test.com", "itfeed3"));
        UserDto user2 = userService.addUser(validUser("it-feed4@test.com", "itfeed4"));

        userService.addFriend(user1.getId(), user2.getId());

        List<EventDto> feed = eventService.getFeed(user1.getId());

        assertThat(feed).hasSize(1);
        assertThat(feed.getFirst().getEventType()).isEqualTo("FRIEND");
        assertThat(feed.getFirst().getOperation()).isEqualTo("ADD");
        assertThat(feed.getFirst().getEntityId()).isEqualTo(user2.getId());
    }

    private FilmDto validFilm() {
        FilmDto dto = new FilmDto();
        dto.setName("Фильм для теста");
        dto.setDescription("Описание");
        dto.setReleaseDate(LocalDate.of(2000, 1, 1));
        dto.setDuration(120L);
        dto.setMpa(new MpaDto(1L));
        return dto;
    }

    private UserDto validUser(String email, String login) {
        UserDto dto = new UserDto();
        dto.setEmail(email);
        dto.setLogin(login);
        dto.setName(login);
        dto.setBirthday(LocalDate.of(1990, 1, 1));
        return dto;
    }
}