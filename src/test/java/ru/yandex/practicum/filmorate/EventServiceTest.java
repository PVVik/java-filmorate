package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
class EventServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private FilmService filmService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private EventService eventService;

    @Test
    void testFeedWithAscendingSorting() {
        UserDto u1 = userService.addUser(new UserDto(0L, "u1@test.com", "login1", "User One", LocalDate.of(1995, 1, 1)));
        UserDto u2 = userService.addUser(new UserDto(0L, "u2@test.com", "login2", "User Two", LocalDate.of(1996, 1, 1)));
        UserDto u3 = userService.addUser(new UserDto(0L, "u3@test.com", "login3", "User Three", LocalDate.of(1997, 1, 1)));

        FilmDto f1 = filmService.addFilm(validFilm("Film A", LocalDate.of(2020, 1, 1)));
        FilmDto f2 = filmService.addFilm(validFilm("Film B", LocalDate.of(2021, 1, 1)));

        long userId = u1.getId();
        long friendId = u3.getId();
        long reviewerId = u2.getId();

        filmService.addLike(f1.getId(), reviewerId);

        ReviewDto reviewDto = reviewService.create(
                new ReviewDto(null, "Отличный фильм!", true, reviewerId, f2.getId(), 0L)
        );
        long reviewId = reviewDto.getReviewId();

        reviewService.update(new ReviewDto(reviewId, "Ещё лучше теперь!", true, reviewerId, f2.getId(), 0L));

        filmService.addLike(f2.getId(), userId);

        userService.addFriend(userId, friendId);

        userService.deleteFriend(userId, friendId);

        userService.addFriend(friendId, userId);

        List<EventDto> feed = eventService.getFeed(userId);

        assertEquals(3, feed.size(), "В ленте должны быть только события пользователя userId");

        assertEquals("LIKE", feed.get(0).getEventType(), "Событие 0: лайк самого пользователя");
        assertEquals("ADD", feed.get(0).getOperation());
        assertEquals(f2.getId(), feed.get(0).getEntityId());
        assertEquals(userId, feed.get(0).getUserId());

        assertEquals("FRIEND", feed.get(1).getEventType(), "Событие 1: добавление друга");
        assertEquals("ADD", feed.get(1).getOperation());
        assertEquals(friendId, feed.get(1).getEntityId());
        assertEquals(userId, feed.get(1).getUserId());

        assertEquals("FRIEND", feed.get(2).getEventType(), "Событие 2: удаление дружбы");
        assertEquals("REMOVE", feed.get(2).getOperation());
        assertEquals(friendId, feed.get(2).getEntityId());
        assertEquals(userId, feed.get(2).getUserId());

        List<EventDto> reviewerFeed = eventService.getFeed(reviewerId);
        boolean hasReviewUpdate = reviewerFeed.stream()
                .anyMatch(e -> "REVIEW".equals(e.getEventType()) && "UPDATE".equals(e.getOperation()));
        assertTrue(hasReviewUpdate, "В ленте автора отзыва должно быть событие REVIEW UPDATE");
    }

    private FilmDto validFilm(String name, LocalDate releaseDate) {
        FilmDto dto = new FilmDto();
        dto.setName(name);
        dto.setDescription("Описание фильма для теста");
        dto.setReleaseDate(releaseDate);
        dto.setDuration(120L);

        MpaDto mpa = new MpaDto();
        mpa.setId(1L);
        mpa.setName("PG-13");
        dto.setMpa(mpa);

        return dto;
    }
}
