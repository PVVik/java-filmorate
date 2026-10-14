package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.VoteType;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.List;

@Repository
@Slf4j
public class ReviewDbStorage extends BaseRepository<Review> implements ReviewStorage {

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Review create(Review review) {
        String query = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                "VALUES (?, ?, ?, ?, ?)";

        log.info("INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                        "VALUES ({}, {}, {}, {}, {})", review.getContent(), review.isPositive(), review.getUserId(),
                review.getFilmId(), review.getUseful());

        long id = insert(query, review.getContent(), review.isPositive(), review.getUserId(),
                review.getFilmId(), review.getUseful());
        review.setReviewId(id);

        return review;
    }

    @Override
    public Review update(Review review) {
        String query = "UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, film_id = ?, useful = ? " +
                "WHERE review_id = ?";

        log.info("UPDATE reviews SET content = {}, is_positive = {}, user_id = {}, film_id = {}, useful = {} " +
                        "WHERE review_id = {}", review.getContent(), review.isPositive(), review.getUserId(), review.getFilmId(),
                review.getUseful(), review.getReviewId());

        update(query, review.getContent(), review.isPositive(), review.getUserId(), review.getFilmId(),
                review.getUseful(), review.getReviewId());

        return review;
    }

    @Override
    public void delete(long reviewId) {
        String query = "DELETE FROM reviews WHERE review_id = ?";

        log.info("DELETE FROM reviews WHERE review_id = {}", reviewId);

        delete(query, reviewId);
    }

    @Override
    public Review getById(long reviewId) {
        String query = "SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, r.useful " +
                "FROM reviews AS r WHERE r.review_id = ?";

        log.info("SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, r.useful " +
                "FROM reviews AS r WHERE r.review_id = {}", reviewId);

        return findOne(query, reviewId).orElseGet(() -> {
                    log.warn("Отзыва с id {} не существует", reviewId);
                    throw new NotFoundException(String.format("Отзыва с id %d не существует", reviewId));
                }
        );
    }

    @Override
    public List<Review> getAllByFilmIdAndLimit(long filmId, long limit) {
        String query = "SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, r.useful " +
                "FROM reviews AS r WHERE r.film_id = ? LIMIT ?";

        log.info("SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, r.useful " +
                "FROM reviews AS r WHERE r.film_id = {} LIMIT {}", filmId, limit);

        return findMany(query, filmId, limit);
    }

    @Override
    public List<Review> getAllByLimit(long limit) {
        String query = "SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, r.useful " +
                "FROM reviews AS r LIMIT ?";

        log.info("SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, r.useful " +
                "FROM reviews AS r LIMIT {}", limit);

        return findMany(query, limit);
    }

    @Override
    public void addVote(long reviewId, long userId, VoteType voteType) {
        String query = "INSERT INTO review_votes (review_id, user_id, vote_type) " +
                "VALUES (?, ?, ?)";

        log.info("INSERT INTO review_votes (review_id, user_id, vote_type) " +
                "VALUES ({}, {}, {})", reviewId, userId, voteType.toString());

        insert(query, reviewId, userId, voteType.toString());
    }

    @Override
    public void deleteVote(long reviewId, long userId, VoteType voteType) {
        String query = "DELETE FROM review_votes WHERE review_id = ? AND user_id = ? AND vote_type = ?";

        log.info("DELETE FROM review_votes WHERE review_id = {} AND user_id = {} AND vote_type = {}",
                reviewId, userId, voteType.toString());

        delete(query, reviewId, userId, voteType.toString());
    }

}
