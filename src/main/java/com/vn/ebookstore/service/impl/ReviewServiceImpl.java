package com.vn.ebookstore.service.impl;

import com.vn.ebookstore.model.Book;
import com.vn.ebookstore.model.Review;
import com.vn.ebookstore.repository.BookRepository;
import com.vn.ebookstore.repository.ReviewRepository;
import com.vn.ebookstore.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Override
    @Transactional
    public Review saveReview(Review review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        Review savedReview = reviewRepository.save(review);

        // Refresh book object to update review calculations
        Book book = review.getBook();
        book.getReviews().add(savedReview);
        bookRepository.save(book);

        return savedReview;
    }

    @Override
    public Optional<Review> findById(Integer id) {
        return reviewRepository.findById(id);
    }

    @Override
    public List<Review> findByBookId(Integer bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    @Override
    public List<Review> findByUserId(Integer userId) {
        return reviewRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteReview(Integer reviewId) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isPresent()) {
            Review review = reviewOpt.get();
            Book book = review.getBook();
            book.getReviews().remove(review);
            reviewRepository.deleteById(reviewId);
            bookRepository.save(book);
        }
    }

    @Override
    public boolean hasUserReviewedBook(Integer userId, Integer bookId) {
        return reviewRepository.existsByUserIdAndBookId(userId, bookId);
    }

    @Override
    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    @Override
    public Page<Review> getReviewsByRatingAndSort(Integer rating, String sort, Pageable pageable) {
        if (rating != null) {
            return reviewRepository.findByRating(rating, pageable);
        }
        if ("newest".equals(sort)) {
            return reviewRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else if ("oldest".equals(sort)) {
            return reviewRepository.findAllByOrderByCreatedAtAsc(pageable);
        } else if ("highest".equals(sort)) {
            return reviewRepository.findAllByOrderByRatingDesc(pageable);
        } else if ("lowest".equals(sort)) {
            return reviewRepository.findAllByOrderByRatingAsc(pageable);
        }
        return getAllReviews(pageable);
    }

    @Override
    public void deleteById(Integer id) {
        reviewRepository.deleteById(id);
    }

    @Override
    public List<Review> findWithFilters(String search, Integer rating, String sort) {
        List<Review> all = reviewRepository.findAll(); // hoặc custom query nếu muốn

        // Lọc theo từ khoá
        if (search != null && !search.isBlank()) {
            all = all.stream().filter(r ->
                    r.getUser().getFullName().toLowerCase().contains(search.toLowerCase())
                            || r.getBook().getTitle().toLowerCase().contains(search.toLowerCase())
            ).collect(Collectors.toList());
        }

        // Lọc theo số sao
        if (rating != null) {
            all = all.stream().filter(r -> r.getRating() == rating).collect(Collectors.toList());
        }

        // Sắp xếp
        Comparator<Review> comparator = Comparator.comparing(Review::getCreatedAt);
        if (sort.equals("oldest")) {
            // giữ nguyên
        } else if (sort.equals("highest")) {
            comparator = Comparator.comparing(Review::getRating).reversed();
        } else if (sort.equals("lowest")) {
            comparator = Comparator.comparing(Review::getRating);
        } else {
            comparator = Comparator.comparing(Review::getCreatedAt).reversed(); // newest
        }
        all.sort(comparator);

        return all;
    }


}