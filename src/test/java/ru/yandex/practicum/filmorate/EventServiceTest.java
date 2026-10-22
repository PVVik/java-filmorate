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
        // 1. Создаём пользователей и фильмы
        UserDto u1 = userService.addUser(new UserDto(0L, "u1@test.com", "login1", "User One", LocalDate.of(1995, 1, 1)));
        UserDto u2 = userService.addUser(new UserDto(0L, "u2@test.com", "login2", "User Two", LocalDate.of(1996, 1, 1)));
        UserDto u3 = userService.addUser(new UserDto(0L, "u3@test.com", "login3", "User Three", LocalDate.of(1997, 1, 1)));

        FilmDto f1 = filmService.addFilm(validFilm("Film A", LocalDate.of(2020, 1, 1)));
        FilmDto f2 = filmService.addFilm(validFilm("Film B", LocalDate.of(2021, 1, 1)));

        long userId = u1.getId();
        long friendId = u3.getId();
        long reviewerId = u2.getId();

        // --- СЦЕНАРИЙ: действия строго по порядку (хронология создания событий) ---

        // Действие 1: Лайк от ДРУГОГО пользователя (НЕ попадает в ленту u1)
        filmService.addLike(f1.getId(), reviewerId);

        // Действие 2: Отзыв от ДРУГОГО пользователя
        ReviewDto reviewDto = reviewService.create(
                new ReviewDto(null, "Отличный фильм!", true, reviewerId, f2.getId(), 0L)
        );
        long reviewId = reviewDto.getReviewId();

        // Действие 3: ОБНОВЛЕНИЕ отзыва (создаёт событие REVIEW UPDATE)
        reviewService.update(new ReviewDto(reviewId, "Ещё лучше теперь!", true, reviewerId, f2.getId(), 0L));

        // Действие 4: Лайк ОТ ГЛАВНОГО пользователя (ПОПАДАЕТ в ленту u1) -> Событие #1 (самое старое в ленте)
        filmService.addLike(f2.getId(), userId);

        // Действие 5: Дружба (ADD) -> Событие #2
        userService.addFriend(userId, friendId);

        // Действие 6: Удаление дружбы (REMOVE) -> Событие #3
        userService.deleteFriend(userId, friendId);

        // Действие 7: Друг добавляет главного пользователя обратно (самое свежее событие) -> Событие #4
        userService.addFriend(friendId, userId);

        // --- ПРОВЕРКА ЛЕНТЫ ГЛАВНОГО ПОЛЬЗОВАТЕЛЯ ---
        List<EventDto> feed = eventService.getFeed(userId);

        System.out.println("=== FEED DEBUG (ASC SORTING) ===");
        feed.forEach(e -> System.out.println(
                e.getEventType() + " | " + e.getOperation() + " | ts: " + e.getTimestamp()
        ));
        System.out.println("===============================");

        assertEquals(4, feed.size(), "В ленте должно быть ровно 4 события");

        // ВАЖНО: При сортировке ASC:
        // feed.get(0) — САМОЕ СТАРОЕ событие
        // feed.get(3) — САМОЕ СВЕЖЕЕ событие

        // feed[0]: Самое СТАРОЕ событие в ленте — LIKE ADD (u1 лайкнул f2)
        assertEquals("LIKE", feed.get(0).getEventType(), "Событие 0: лайк самого пользователя");
        assertEquals("ADD", feed.get(0).getOperation());
        assertEquals(f2.getId(), feed.get(0).getEntityId());

        // feed[1]: FRIEND ADD (u1 добавил u3)
        assertEquals("FRIEND", feed.get(1).getEventType(), "Событие 1: добавление друга");
        assertEquals("ADD", feed.get(1).getOperation());
        assertEquals(friendId, feed.get(1).getEntityId());

        // feed[2]: FRIEND REMOVE (u1 удалил u3)
        assertEquals("FRIEND", feed.get(2).getEventType(), "Событие 2: удаление дружбы");
        assertEquals("REMOVE", feed.get(2).getOperation());
        assertEquals(friendId, feed.get(2).getEntityId());

        // Дополнительная проверка: в ленте автора отзыва есть событие UPDATE
        List<EventDto> reviewerFeed = eventService.getFeed(reviewerId);
        boolean hasReviewUpdate = reviewerFeed.stream()
                .anyMatch(e -> "REVIEW".equals(e.getEventType()) && "UPDATE".equals(e.getOperation()));
        assertTrue(hasReviewUpdate, "В ленте автора отзыва должно быть событие REVIEW UPDATE");
    }

    // Вспомогательный метод для создания фильма
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
