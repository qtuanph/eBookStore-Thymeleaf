package com.vn.ebookstore.controller.admin;

import com.vn.ebookstore.model.Review;
import com.vn.ebookstore.model.User;
import com.vn.ebookstore.model.Book;
import com.vn.ebookstore.service.ReviewService;
import com.vn.ebookstore.service.UserService;
import com.vn.ebookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @GetMapping
    public String listReviews(@RequestParam(required = false) String rating,
                              @RequestParam(required = false) String sort,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewsPage = reviewService.getReviewsByRatingAndSort(rating != null ? Integer.parseInt(rating) : null, sort, pageable);
        model.addAttribute("reviews", reviewsPage);
        model.addAttribute("ratings", Arrays.asList("1", "2", "3", "4", "5"));
        model.addAttribute("sortOptions", Arrays.asList("newest", "oldest", "highest", "lowest"));
        return "page/admin/reviews/list-reviews";
    }

    @GetMapping("/{id}")
    public String viewReview(@PathVariable Integer id, Model model) {
        Review review = reviewService.findById(id).orElse(null);
        if (review == null) {
            return "redirect:/admin/reviews?error=not-found";
        }
        User user = review.getUser();
        Book book = review.getBook();
        model.addAttribute("review", review);
        model.addAttribute("user", user);
        model.addAttribute("book", book);
        return "page/admin/reviews/view-review"; // Sửa đường dẫn trả về
    }

    @GetMapping("/delete/{id}")
    public String deleteReview(@PathVariable Integer id) {
        reviewService.deleteReview(id);
        return "redirect:/admin/reviews?success=deleted";
    }
}