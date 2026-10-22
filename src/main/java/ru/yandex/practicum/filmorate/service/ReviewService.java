package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.VoteType;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmService filmService;
    private final UserService userService;
    private final EventService eventService;

    @Transactional
    public ReviewDto create(ReviewDto reviewDto) {
        userService.checkUserExists(reviewDto.getUserId());
        filmService.checkFilmExists(reviewDto.getFilmId());

        var review = reviewStorage.create(ReviewMapper.mapToEntity(reviewDto));
        eventService.addReviewEvent(review.getUserId(), review.getReviewId(), Operation.ADD);

        log.info("Создали отзыв с id {}", review.getReviewId());
        return ReviewMapper.mapToDto(review);
    }

    @Transactional
    public ReviewDto update(ReviewDto reviewDto) {
        if (reviewDto.getFilmId() != null) {
            filmService.checkFilmExists(reviewDto.getFilmId());
        }
        if (reviewDto.getUserId() != null) {
            userService.checkUserExists(reviewDto.getUserId());
        }

        var oldReview = reviewStorage.getById(reviewDto.getReviewId());
        var updateReview = ReviewMapper.mapToUpdateEntity(oldReview, reviewDto);

        var savedReview = reviewStorage.update(updateReview);
        eventService.addReviewEvent(savedReview.getUserId(), savedReview.getReviewId(), Operation.UPDATE);

        log.info("Обновили отзыв с id {}", savedReview.getReviewId());
        return ReviewMapper.mapToDto(savedReview);
    }

    public ReviewDto getById(long id) {
        var review = reviewStorage.getById(id);
        log.info("Получили отзыв с id {}", id);

        return ReviewMapper.mapToDto(review);
    }

    public List<ReviewDto> search(Long filmId, long limit) {
        if (filmId != null) {
            filmService.checkFilmExists(filmId);

            log.info("Получили список отзывов к фильму с id {} в количестве {}", filmId, limit);

            return reviewStorage.getAllByFilmIdAndLimit(filmId, limit).stream()
                    .sorted(Review::compareTo)
                    .map(ReviewMapper::mapToDto)
                    .toList();
        }
        return reviewStorage.getAllByLimit(limit).stream()
                .sorted(Review::compareTo)
                .map(ReviewMapper::mapToDto)
                .toList();
    }

    @Transactional
    public void delete(long id) {
        var review = reviewStorage.getById(id);

        reviewStorage.delete(review.getReviewId());

        eventService.addReviewEvent(review.getUserId(), review.getReviewId(), Operation.REMOVE);

        log.info("Удалили отзыв с id {} и создали событие REMOVE", review.getReviewId());
    }


    public void likeReview(long reviewId, long userId) {
        var review = reviewStorage.getById(reviewId);
        userService.checkUserExists(userId);

        if (reviewStorage.findVoteType(reviewId, userId).isPresent()) {
            reviewStorage.updateVote(reviewId, userId, VoteType.LIKE);
            changeUsefulIfAddVote(review, VoteType.LIKE);
        } else {
            reviewStorage.addVote(reviewId, userId, VoteType.LIKE);
        }

        changeUsefulIfAddVote(review, VoteType.LIKE);
        reviewStorage.update(review);

        log.info("Лайкнули отзыв с id {}, сейчас оценка отзыва {}", review.getReviewId(), review.getUseful());
    }

    public void dislikeReview(long reviewId, long userId) {
        var review = reviewStorage.getById(reviewId);
        userService.checkUserExists(userId);

        if (reviewStorage.findVoteType(reviewId, userId).isPresent()) {
            reviewStorage.updateVote(reviewId, userId, VoteType.DISLIKE);
            changeUsefulIfAddVote(review, VoteType.DISLIKE);
        } else {
            reviewStorage.addVote(reviewId, userId, VoteType.DISLIKE);
        }

        changeUsefulIfAddVote(review, VoteType.DISLIKE);
        reviewStorage.update(review);

        log.info("Дизлайкнули отзыв с id {}, сейчас оценка отзыва {}", review.getReviewId(), review.getUseful());
    }

    public void deleteLikeReview(long reviewId, long userId) {
        var review = reviewStorage.getById(reviewId);
        userService.checkUserExists(userId);

        reviewStorage.deleteVote(reviewId, userId, VoteType.LIKE);

        changeUsefulIfDeleteVote(review, VoteType.LIKE);
        reviewStorage.update(review);

        log.info("Удалили лайк отзыв с id {}, сейчас оценка отзыва {}", review.getReviewId(), review.getUseful());
    }

    public void deleteDislikeReview(long reviewId, long userId) {
        var review = reviewStorage.getById(reviewId);
        userService.checkUserExists(userId);

        reviewStorage.deleteVote(reviewId, userId, VoteType.DISLIKE);

        changeUsefulIfDeleteVote(review, VoteType.DISLIKE);
        reviewStorage.update(review);

        log.info("Удалили дизлайк отзыв с id {}, сейчас оценка отзыва {}", review.getReviewId(), review.getUseful());
    }

    private void changeUsefulIfAddVote(Review review, VoteType voteType) {
        long useful = review.getUseful();

        if (voteType.equals(VoteType.LIKE)) {
            useful = useful + 1;
        }
        if (voteType.equals((VoteType.DISLIKE))) {
            useful = useful - 1;
        }

        review.setUseful(useful);
    }

    private void changeUsefulIfDeleteVote(Review review, VoteType voteType) {
        long useful = review.getUseful();

        if (voteType.equals(VoteType.LIKE)) {
            useful = useful - 1;
        }
        if (voteType.equals((VoteType.DISLIKE))) {
            useful = useful + 1;
        }

        review.setUseful(useful);
    }

}
