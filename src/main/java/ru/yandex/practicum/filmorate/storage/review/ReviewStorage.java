package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.VoteType;

import java.util.List;

public interface ReviewStorage {

    Review create(Review review);

    Review update(Review review);

    void delete(long reviewId);

    Review getById(long reviewId);

    List<Review> getAllByFilmIdAndLimit(long filmId, long limit);

    List<Review> getAllByLimit(long limit);

    void addVote(long reviewId, long userId, VoteType voteType);

    void deleteVote(long reviewId, long userId, VoteType voteType);
}
