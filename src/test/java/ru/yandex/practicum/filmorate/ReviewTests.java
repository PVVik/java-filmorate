package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.VoteType;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewRowMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase
@Import({ReviewDbStorage.class, ReviewRowMapper.class})
class ReviewTests {

    @Autowired
    private ReviewDbStorage reviewStorage;

    @Autowired
    private JdbcTemplate jdbc;

    private static final String CONTENT_INITIAL = "Initial content";
    private static final String CONTENT_UPDATED = "Updated content";

    @Test
    @DisplayName("create: должен успешно добавить отзыв с корректными зависимостями")
    public void createReview_shouldAddReview() {
        Review saved = createReviewWithFullDependencies(CONTENT_INITIAL, true, 0L);

        assertThat(saved.getReviewId()).isNotNull();
        assertThat(saved.getReviewId()).isGreaterThan(0);
        assertThat(saved.getContent()).isEqualTo(CONTENT_INITIAL);
        assertThat(saved.isPositive()).isTrue();
        assertThat(saved.getFilmId()).isGreaterThan(0);
        assertThat(saved.getUserId()).isGreaterThan(0);
    }

    @Test
    @DisplayName("update: должен обновить данные отзыва")
    public void updateReview_shouldUpdateReview() {
        Review saved = createReviewWithFullDependencies(CONTENT_INITIAL, true, 0L);

        for (int i = 0; i < 3; i++) {
            long voterId = createTestUser("voter" + i + "@test.com", "voter" + i, "Voter " + i,
                    LocalDate.of(1990 + i, 1, 1));
            reviewStorage.addVote(saved.getReviewId(), voterId, VoteType.LIKE);
        }
        long dislikerId = createTestUser("disliker@test.com", "disliker", "Disliker",
                LocalDate.of(1995, 1, 1));
        reviewStorage.addVote(saved.getReviewId(), dislikerId, VoteType.DISLIKE);

        saved.setContent(CONTENT_UPDATED);
        saved.setPositive(false);

        Review updated = reviewStorage.update(saved);

        assertThat(updated.getContent()).isEqualTo(CONTENT_UPDATED);
        assertThat(updated.isPositive()).isFalse();
        assertThat(updated.getUseful()).isEqualTo(2L);
    }

    @Test
    @DisplayName("getById: должен вернуть отзыв по ID")
    public void getById_shouldReturnReview() {
        Review saved = createReviewWithFullDependencies(CONTENT_INITIAL, true, 0L);
        Review found = reviewStorage.getById(saved.getReviewId());

        assertThat(found.getReviewId()).isEqualTo(saved.getReviewId());
        assertThat(found.getContent()).isEqualTo(CONTENT_INITIAL);
    }

    @Test
    @DisplayName("getById: должен выбросить NotFoundException для несуществующего ID")
    public void getById_shouldThrow_whenReviewNotFound() {
        assertThatThrownBy(() -> reviewStorage.getById(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не существует");
    }

    @Test
    @DisplayName("getAllByFilmIdAndLimit: должен вернуть отзывы по filmId с ограничением")
    public void getAllByFilmIdAndLimit_shouldReturnFilteredList() {
        long mpaId = createOrGetMpa("PG-13");

        long filmId1 = createTestFilm("Film A", "Desc A", LocalDate.of(2001, 1, 1), 90L, mpaId);
        long filmId2 = createTestFilm("Film B", "Desc B", LocalDate.of(2002, 1, 1), 100L, mpaId);

        long u1 = createTestUser("u1@test.com", "u1", "User 1", LocalDate.of(1990, 1, 1));
        long u2 = createTestUser("u2@test.com", "u2", "User 2", LocalDate.of(1991, 1, 1));

        reviewStorage.create(Review.builder().content("Rev A1").isPositive(true).userId(u1).filmId(filmId1).useful(0L).build());
        reviewStorage.create(Review.builder().content("Rev A2").isPositive(false).userId(u2).filmId(filmId1).useful(1L).build());
        reviewStorage.create(Review.builder().content("Rev B1").isPositive(true).userId(u1).filmId(filmId2).useful(2L).build());

        List<Review> result = reviewStorage.getAllByFilmIdAndLimit(filmId1, 1);

        assertEquals(1, result.size());
        assertThat(result.get(0).getFilmId()).isEqualTo(filmId1);
    }

    @Test
    @DisplayName("getAllByLimit: должен вернуть список отзывов с ограничением")
    public void getAllByLimit_shouldReturnLimitedList() {
        long mpaId = createOrGetMpa("R");
        long f1 = createTestFilm("F1", "D1", LocalDate.of(2010, 1, 1), 80L, mpaId);
        long f2 = createTestFilm("F2", "D2", LocalDate.of(2011, 1, 1), 90L, mpaId);

        long u1 = createTestUser("x1@test.com", "x1", "X1", LocalDate.of(1980, 1, 1));
        long u2 = createTestUser("x2@test.com", "x2", "X2", LocalDate.of(1981, 1, 1));
        long u3 = createTestUser("x3@test.com", "x3", "X3", LocalDate.of(1982, 1, 1));

        reviewStorage.create(Review.builder().content("R1").isPositive(true).userId(u1).filmId(f1).useful(0L).build());
        reviewStorage.create(Review.builder().content("R2").isPositive(false).userId(u2).filmId(f2).useful(1L).build());
        reviewStorage.create(Review.builder().content("R3").isPositive(true).userId(u3).filmId(f1).useful(2L).build());

        List<Review> result = reviewStorage.getAllByLimit(2);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("addVote: должен добавить голос за отзыв")
    public void addVote_shouldInsertVote() {
        Review review = createReviewWithFullDependencies("Vote Test", true, 0L);
        long userId = createTestUser("voter@test.com", "voter", "Voter", LocalDate.of(1995, 1, 1));

        reviewStorage.addVote(review.getReviewId(), userId, VoteType.LIKE);

        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM review_votes WHERE review_id = ? AND user_id = ? AND vote_type = ?",
                Long.class, review.getReviewId(), userId, VoteType.LIKE.toString());

        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("deleteVote: должен удалить голос за отзыв")
    public void deleteVote_shouldRemoveVote() {
        Review review = createReviewWithFullDependencies("Delete Vote Test", true, 0L);
        long userId = createTestUser("delvoter@test.com", "delvoter", "Del Voter", LocalDate.of(1996, 1, 1));

        reviewStorage.addVote(review.getReviewId(), userId, VoteType.DISLIKE);

        Long before = jdbc.queryForObject(
                "SELECT COUNT(*) FROM review_votes WHERE review_id = ? AND user_id = ? AND vote_type = ?",
                Long.class, review.getReviewId(), userId, VoteType.DISLIKE.toString());
        assertThat(before).isEqualTo(1L);

        reviewStorage.deleteVote(review.getReviewId(), userId, VoteType.DISLIKE);

        Long after = jdbc.queryForObject(
                "SELECT COUNT(*) FROM review_votes WHERE review_id = ? AND user_id = ? AND vote_type = ?",
                Long.class, review.getReviewId(), userId, VoteType.DISLIKE.toString());
        assertThat(after).isEqualTo(0L);
    }

    @Test
    @DisplayName("delete: должен удалить отзыв из БД")
    public void delete_shouldRemoveReview() {
        Review saved = createReviewWithFullDependencies(CONTENT_INITIAL, true, 0L);
        reviewStorage.delete(saved.getReviewId());

        assertThatThrownBy(() -> reviewStorage.getById(saved.getReviewId()))
                .isInstanceOf(NotFoundException.class);
    }

    private long createOrGetMpa(String mpaName) {
        Long id = jdbc.queryForObject(
                "SELECT mpa_id FROM mpa WHERE mpa_name = ?",
                Long.class, mpaName);
        if (id != null) {
            return id;
        }

        jdbc.update("INSERT INTO mpa (mpa_name) VALUES (?)", mpaName);
        return jdbc.queryForObject("SELECT MAX(mpa_id) FROM mpa", Long.class);
    }

    private long createTestFilm(String filmName, String description, LocalDate releaseDate, long duration, long mpaId) {
        String sql = "INSERT INTO films (film_name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql, filmName, description, releaseDate, duration, mpaId);
        return jdbc.queryForObject("SELECT MAX(film_id) FROM films", Long.class);
    }

    private long createTestUser(String email, String login, String userName, LocalDate birthday) {
        String sql = "INSERT INTO users (email, login, user_name, birthday) VALUES (?, ?, ?, ?)";
        jdbc.update(sql, email, login, userName, birthday);
        return jdbc.queryForObject("SELECT MAX(user_id) FROM users", Long.class);
    }

    private Review createReviewWithFullDependencies(String content, boolean isPositive, long useful) {
        long mpaId = createOrGetMpa("PG");

        long filmId = createTestFilm(
                "Test Film for Review",
                "Description for test film",
                LocalDate.of(2000, 1, 1),
                120L,
                mpaId
        );

        long userId = createTestUser(
                "review_user@test.com",
                "rev_user",
                "Review User",
                LocalDate.of(1999, 1, 1)
        );

        Review review = Review.builder()
                .content(content)
                .isPositive(isPositive)
                .userId(userId)
                .filmId(filmId)
                .useful(useful)
                .build();

        return reviewStorage.create(review);
    }

}
