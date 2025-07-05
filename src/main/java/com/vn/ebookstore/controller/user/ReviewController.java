package com.vn.ebookstore.controller.user;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.ebookstore.model.Book;
import com.vn.ebookstore.model.Cart;
import com.vn.ebookstore.model.Category;
import com.vn.ebookstore.model.Review;
import com.vn.ebookstore.model.User;
import com.vn.ebookstore.model.Wishlist;
import com.vn.ebookstore.service.BookService;
import com.vn.ebookstore.service.CartService;
import com.vn.ebookstore.service.CategoryService;
import com.vn.ebookstore.service.ReviewService;
import com.vn.ebookstore.service.UserService;
import com.vn.ebookstore.service.WishlistService;

@Controller
@RequestMapping("/user")
public class ReviewController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private UserService userService;
    @Autowired
    private CartService cartService;
    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private BookService bookService;

    @PostMapping("/review/add")
    public String addReview(
            @RequestParam("bookId") Integer bookId,
            @RequestParam("rating") Integer rating,
            @RequestParam("comment") String comment,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            if (principal == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đánh giá");
                return "redirect:/login";
            }

            User user = userService.getUserByEmail(principal.getName());

            if (reviewService.hasUserReviewedBook(user.getId(), bookId)) {
                redirectAttributes.addFlashAttribute("error", "Bạn đã đánh giá cuốn sách này rồi");
                return "redirect:/user/book/" + bookId;
            }

            if (rating < 1 || rating > 5) {
                redirectAttributes.addFlashAttribute("error", "Đánh giá không hợp lệ");
                return "redirect:/user/book/" + bookId;
            }

            Book book = bookService.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));

            Review review = new Review();
            review.setUser(user);
            review.setBook(book);
            review.setRating(rating);
            review.setComment(comment);
            review.setCreatedAt(new Date());

            reviewService.saveReview(review);

            book.updateAverageRating();
            bookService.save(book);

            redirectAttributes.addFlashAttribute("success", "Đã thêm đánh giá thành công");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm đánh giá: " + e.getMessage());
        }
        return "redirect:/user/book/" + bookId;
    }

    @PutMapping("/review/update/{reviewId}")
    @ResponseBody
    public ResponseEntity<?> updateReview(
            @PathVariable("reviewId") Integer reviewId,
            @RequestParam("rating") Integer rating,
            @RequestParam("comment") String comment,
            Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            Review review = reviewService.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

            if (!Objects.equals(review.getUser().getId(), user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Không có quyền sửa đánh giá này"));
            }

            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest().body(Map.of("error", "Đánh giá phải từ 1 đến 5 sao"));
            }

            review.setRating(rating);
            review.setComment(comment);
            review.setUpdatedAt(new Date());

            Review updatedReview = reviewService.saveReview(review);
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "review", updatedReview,
                    "message", "Đã cập nhật đánh giá thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/review/delete/{reviewId}")
    @ResponseBody
    public ResponseEntity<?> deleteReview(
            @PathVariable("reviewId") Integer reviewId,
            Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            Review review = reviewService.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

            if (!Objects.equals(review.getUser().getId(), user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Không có quyền xóa đánh giá này"));
            }

            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Đã xóa đánh giá thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/reviews")
    public String showUserReviews(Model model, Principal principal) {
        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            List<Category> categories = categoryService.getAllCategories();
            Cart cart = cartService.getCurrentCartByUser(user);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            List<Review> reviews = reviewService.findByUserId(user.getId());

            model.addAttribute("categories", categories);
            model.addAttribute("cart", cart);
            model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
            model.addAttribute("reviews", reviews);

            return "page/user/review";
        }
        return "redirect:/login";
    }
}
