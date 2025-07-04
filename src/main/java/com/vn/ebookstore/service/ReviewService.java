package com.vn.ebookstore.service;

import com.vn.ebookstore.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    Review saveReview(Review review);
    Optional<Review> findById(Integer id);
    List<Review> findByBookId(Integer bookId);
    List<Review> findByUserId(Integer userId);
    void deleteReview(Integer reviewId);
    boolean hasUserReviewedBook(Integer userId, Integer bookId);

    // Thêm phương thức mới
    Page<Review> getAllReviews(Pageable pageable);
    Page<Review> getReviewsByRatingAndSort(Integer rating, String sort, Pageable pageable);

    void deleteById(Integer id);
    List<Review> findWithFilters(String search, Integer rating, String sort);

}