package com.vn.ebookstore.controller.admin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.ebookstore.model.Book;
import com.vn.ebookstore.model.Review;
import com.vn.ebookstore.model.User;
import com.vn.ebookstore.service.BookService;
import com.vn.ebookstore.service.ReviewService;
import com.vn.ebookstore.service.UserService;

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
        Integer ratingValue = null;
        try {
            if (rating != null && !rating.isEmpty()) {
                ratingValue = Integer.parseInt(rating);
            }
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Giá trị rating không hợp lệ");
            return "page/admin/reviews/list-reviews";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewsPage = reviewService.getReviewsByRatingAndSort(ratingValue, sort, pageable);

        model.addAttribute("reviews", reviewsPage);
        model.addAttribute("ratings", Arrays.asList("1", "2", "3", "4", "5"));
        model.addAttribute("sortOptions", Arrays.asList("newest", "oldest", "highest", "lowest"));
        return "page/admin/reviews/list-reviews";
    }

    @GetMapping("/{id}")
    public String viewReview(@PathVariable Integer id, Model model) {
        Optional<Review> optionalReview = reviewService.findById(id);
        if (optionalReview.isEmpty()) {
            return "redirect:/admin/reviews?error=not-found";
        }

        Review review = optionalReview.get();
        User user = review.getUser();
        Book book = review.getBook();

        model.addAttribute("review", review);
        model.addAttribute("user", user);
        model.addAttribute("book", book);
        return "page/admin/reviews/view-review";
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa đánh giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa đánh giá thất bại.");
        }
        return "redirect:/admin/reviews";
    }

    @GetMapping("/detail/{id}")
    @ResponseBody
    public Map<String, Object> getReviewDetail(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        Optional<Review> optionalReview = reviewService.findById(id);

        if (optionalReview.isEmpty()) {
            response.put("error", "Không tìm thấy đánh giá");
            return response;
        }

        Review review = optionalReview.get();
        User user = review.getUser();
        Book book = review.getBook();

        response.put("id", review.getId());
        response.put("rating", review.getRating());
        response.put("comment", review.getComment());
        response.put("createdAt", review.getCreatedAt() != null ? review.getCreatedAt().toString() : "");
        response.put("updatedAt", review.getUpdatedAt() != null ? review.getUpdatedAt().toString() : "");

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("fullName", user.getFullName());
        userMap.put("email", user.getEmail());

        Map<String, Object> bookMap = new HashMap<>();
        bookMap.put("id", book.getId());
        bookMap.put("title", book.getTitle());
        bookMap.put("author", book.getAuthor());

        response.put("user", userMap);
        response.put("book", bookMap);

        return response;
    }
}
