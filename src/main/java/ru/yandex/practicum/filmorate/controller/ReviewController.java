package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ReviewDto create(@Valid @RequestBody ReviewDto reviewDto) {
        return reviewService.create(reviewDto);
    }

    @PutMapping
    public ReviewDto update(@RequestBody ReviewDto reviewDto) {
        return reviewService.update(reviewDto);
    }

    @GetMapping("/{id}")
    public ReviewDto getById(@PathVariable long id) {
        return reviewService.getById(id);
    }

    @GetMapping
    public List<ReviewDto> search(@RequestParam(required = false) String filmId,
                                  @RequestParam(defaultValue = "10") String limit) {
        Long fId = (filmId != null) ? Long.parseLong(filmId) : null;
        return reviewService.search(fId, Long.parseLong(limit));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        reviewService.delete(id);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public void likeReview(@PathVariable long reviewId, @PathVariable long userId) {
        reviewService.likeReview(reviewId, userId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public void dislikeReview(@PathVariable long reviewId, @PathVariable long userId) {
        reviewService.dislikeReview(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public void deleteLikeReview(@PathVariable long reviewId, @PathVariable long userId) {
        reviewService.deleteLikeReview(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public void deleteDislikeReview(@PathVariable long reviewId, @PathVariable long userId) {
        reviewService.deleteDislikeReview(reviewId, userId);
    }
}
