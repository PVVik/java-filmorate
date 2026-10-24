package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Operation;
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

        reviewDto.setUseful(0L);
        var review = reviewStorage.create(ReviewMapper.mapToEntity(reviewDto));
        eventService.addReviewEvent(review.getUserId(), review.getReviewId(), Operation.ADD);

        log.info("Создали отзыв с id {}", review.getReviewId());
        return ReviewMapper.mapToDto(review);
    }

    @Transactional
    public ReviewDto update(ReviewDto reviewDto) {
        var oldReview = reviewStorage.getById(reviewDto.getReviewId());

        oldReview.setContent(reviewDto.getContent());
        oldReview.setPositive(reviewDto.getIsPositive());

        var savedReview = reviewStorage.update(oldReview);
        eventService.addReviewEvent(savedReview.getUserId(), savedReview.getReviewId(), Operation.UPDATE);

        log.info("Обновили отзыв с id {}", savedReview.getReviewId());
        return ReviewMapper.mapToDto(savedReview);
    }

    public ReviewDto getById(long id) {
        var review = reviewStorage.getById(id);
        log.info("Получили отзыв с id {}", id);

        return ReviewMapper.mapToDto(review);
    }

    public List<ReviewDto> getReviewsByFilmId(Long filmId, int count) {
        if (filmId != null) {
            filmService.checkFilmExists(filmId);
            log.info("Получили отзывы к фильму {} в количестве {}", filmId, count);

            return reviewStorage.getAllByFilmIdAndLimit(filmId, count).stream()
                    .map(ReviewMapper::mapToDto)
                    .toList();
        }
        return reviewStorage.getAllByLimit(count).stream()
                .map(ReviewMapper::mapToDto)
                .toList();
    }

    @Transactional
    public void delete(long id) {
        checkReviewExists(id);
        var review = reviewStorage.getById(id);

        reviewStorage.delete(review.getReviewId());

        eventService.addReviewEvent(review.getUserId(), review.getReviewId(), Operation.REMOVE);

        log.info("Удалили отзыв с id {} и создали событие REMOVE", review.getReviewId());
    }

    public void likeReview(long reviewId, long userId) {
        checkReviewExists(reviewId);
        userService.checkUserExists(userId);

        var existingVote = reviewStorage.findVoteType(reviewId, userId);
        if (existingVote.filter(VoteType.LIKE.toString()::equals).isPresent()) {
            return;
        }

        if (existingVote.isPresent()) {
            reviewStorage.updateVote(reviewId, userId, VoteType.LIKE);
        } else {
            reviewStorage.addVote(reviewId, userId, VoteType.LIKE);
        }
        log.info("Лайкнули отзыв с id {}", reviewId);
    }

    public void dislikeReview(long reviewId, long userId) {
        checkReviewExists(reviewId);
        userService.checkUserExists(userId);

        var existingVote = reviewStorage.findVoteType(reviewId, userId);
        if (existingVote.filter(VoteType.DISLIKE.toString()::equals).isPresent()) {
            return;
        }

        if (existingVote.isPresent()) {
            reviewStorage.updateVote(reviewId, userId, VoteType.DISLIKE);
        } else {
            reviewStorage.addVote(reviewId, userId, VoteType.DISLIKE);
        }
        log.info("Дизлайкнули отзыв с id {}", reviewId);
    }

    public void deleteLikeReview(long reviewId, long userId) {
        checkReviewExists(reviewId);
        userService.checkUserExists(userId);

        var existingVote = reviewStorage.findVoteType(reviewId, userId);
        if (existingVote.filter(VoteType.LIKE.toString()::equals).isEmpty()) return;

        reviewStorage.deleteVote(reviewId, userId, VoteType.LIKE);
        log.info("Удалили лайк отзыва с id {}", reviewId);
    }

    public void deleteDislikeReview(long reviewId, long userId) {
        checkReviewExists(reviewId);
        userService.checkUserExists(userId);

        var existingVote = reviewStorage.findVoteType(reviewId, userId);
        if (existingVote.filter(VoteType.DISLIKE.toString()::equals).isEmpty()) return;

        reviewStorage.deleteVote(reviewId, userId, VoteType.DISLIKE);
        log.info("Удалили дизлайк отзыва с id {}", reviewId);
    }

    private void checkReviewExists(long id) {
        reviewStorage.getById(id);
    }
}
