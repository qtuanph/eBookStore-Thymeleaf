package com.vn.ebookstore.controller.user;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.ebookstore.model.Address;
import com.vn.ebookstore.model.Book;
import com.vn.ebookstore.model.Cart;
import com.vn.ebookstore.model.Category;
import com.vn.ebookstore.model.Coupon;
import com.vn.ebookstore.model.OrderDetail;
import com.vn.ebookstore.model.PaymentDetail;
import com.vn.ebookstore.model.Review;
import com.vn.ebookstore.model.User;
import com.vn.ebookstore.model.Wishlist;
import com.vn.ebookstore.security.UserDetailsImpl;
import com.vn.ebookstore.service.AddressService;
import com.vn.ebookstore.service.BookService;
import com.vn.ebookstore.service.CartService;
import com.vn.ebookstore.service.CategoryService;
import com.vn.ebookstore.service.CouponService;
import com.vn.ebookstore.service.OrderDetailService;
import com.vn.ebookstore.service.PaymentDetailService;
import com.vn.ebookstore.service.ReviewService;
import com.vn.ebookstore.service.UserService;
import com.vn.ebookstore.service.WishlistService;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BookService bookService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private CartService cartService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private PaymentDetailService paymentDetailService;
    @Autowired
    private CouponService couponService;

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        List<Category> featuredCategories = categories.subList(0, Math.min(3, categories.size()));
        List<Book> latestBooks = bookService.getLatestBooks();
        List<Book> bestSellers = bookService.getBestSellers();
        List<Book> premiumBooks = bookService.getPremiumBooks();

        model.addAttribute("categories", categories);
        model.addAttribute("featuredCategories", featuredCategories);
        model.addAttribute("latestBooks", latestBooks);
        model.addAttribute("bestSellers", bestSellers);
        model.addAttribute("premiumBooks", premiumBooks);
        model.addAttribute("defaultCoverUrl", "/image/covers/default-cover.jpg");
        model.addAttribute("defaultAvatarUrl", "/image/avatar/default-avatar.jpg");

        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            if (cart == null) {
                cart = new Cart();
                cart.setUser(user);
                cart.setCartItems(new ArrayList<>());
                cart = cartService.save(cart);
            }
            model.addAttribute("cart", cart);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
            return "index";
        }
        return "redirect:/login";
    }

    @GetMapping("/about_us")
    public String aboutUs(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            model.addAttribute("cart", cart);
            model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
        }
        return "page/user/about_us";
    }

    @GetMapping("/faq")
    public String faq(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            model.addAttribute("cart", cart);
            model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
        }
        return "page/user/faq";
    }

    @GetMapping("/order_tracking")
    public String orderTracking(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            model.addAttribute("cart", cart);
            model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
        }
        return "page/user/order_tracking";
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer subCategoryId,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) Float minRating,
            @RequestParam(required = false, defaultValue = "newest") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir,
            Model model,
            Principal principal) {

        Double minPriceDouble = null;
        Double maxPriceDouble = null;
        try {
            if (minPrice != null && !minPrice.trim().isEmpty()) {
                minPriceDouble = Double.valueOf(minPrice.replace(",", ""));
            }
            if (maxPrice != null && !maxPrice.trim().isEmpty()) {
                maxPriceDouble = Double.valueOf(maxPrice.replace(",", ""));
            }
        } catch (NumberFormatException e) {
            logger.warn("Error parsing price values: " + e.getMessage());
            minPriceDouble = 0.0;
            maxPriceDouble = null;
        }

        if (minPriceDouble != null && minPriceDouble < 0) minPriceDouble = 0.0;
        if (maxPriceDouble != null && maxPriceDouble < 0) maxPriceDouble = null;
        if (minPriceDouble != null && maxPriceDouble != null && minPriceDouble > maxPriceDouble) {
            Double temp = minPriceDouble;
            minPriceDouble = maxPriceDouble;
            maxPriceDouble = temp;
        }

        if (!Arrays.asList("newest", "price", "rating").contains(sortBy)) {
            sortBy = "newest";
        }
        if (!Arrays.asList("asc", "desc").contains(sortDir)) {
            sortDir = "desc";
        }

        List<Category> categories = categoryService.getAllCategories();
        List<Book> books = bookService.filterAndSortBooks(
                categoryId,
                subCategoryId,
                minPriceDouble,
                maxPriceDouble,
                sortBy,
                sortDir,
                minRating
        );

        Double lowestPrice = bookService.getLowestPrice();
        Double highestPrice = bookService.getHighestPrice();

        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            model.addAttribute("cart", cart);
            model.addAttribute("wishlists", wishlists);
        }

        model.addAttribute("categories", categories);
        model.addAttribute("books", books);
        model.addAttribute("lowestPrice", lowestPrice);
        model.addAttribute("highestPrice", highestPrice);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedSubCategoryId", subCategoryId);
        model.addAttribute("selectedMinPrice", minPriceDouble);
        model.addAttribute("selectedMaxPrice", maxPriceDouble);
        model.addAttribute("selectedMinRating", minRating);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "page/user/product";
    }

    @GetMapping("/product_detail")
    public String productDetail(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            model.addAttribute("cart", cart);
            model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
        }
        return "page/user/product_detail";
    }

    @GetMapping("/book/{id}")
    public String viewBookDetail(@PathVariable Integer id, Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        Optional<Book> bookOptional = bookService.findById(id);
        if (bookOptional.isPresent()) {
            Book book = bookOptional.get();
            model.addAttribute("book", book);

            if (principal != null) {
                User user = userService.getUserByEmail(principal.getName());
                Cart cart = cartService.getCurrentCartByUser(user);
                if (cart == null) {
                    cart = new Cart();
                    cart.setUser(user);
                    cart.setCartItems(new ArrayList<>());
                    cart = cartService.save(cart);
                }
                model.addAttribute("cart", cart);
                List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
                model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
            }

            return "page/user/product_detail";
        } else {
            return "redirect:/user/products";
        }
    }

    @GetMapping("/cart")
    public String cart(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            model.addAttribute("cart", cart);
            model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
        }
        return "page/user/cart";
    }

    @PostMapping("/cart/add/{bookId}")
    @ResponseBody
    public ResponseEntity<?> addToCart(@PathVariable("bookId") int bookId,
                                       @RequestParam("quantity") int quantity,
                                       Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            cartService.addToCart(user.getId(), bookId, quantity);
            Cart cart = cartService.getCurrentCart(user.getId());
            return ResponseEntity.ok().body(Map.of(
                    "total", cart.getTotal(),
                    "itemCount", cart.getCartItems().size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cart/remove/{bookId}")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@PathVariable("bookId") int bookId,
                                            Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            cartService.removeFromCart(user.getId(), bookId);
            Cart cart = cartService.getCurrentCart(user.getId());
            return ResponseEntity.ok().body(Map.of(
                    "total", cart.getTotal(),
                    "itemCount", cart.getCartItems().size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cart/update/{bookId}")
    @ResponseBody
    public ResponseEntity<?> updateCartItemQuantity(
            @PathVariable("bookId") int bookId,
            @RequestParam("quantity") int quantity,
            Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            cartService.updateCartItemQuantity(user.getId(), bookId, quantity);
            Cart cart = cartService.getCurrentCart(user.getId());
            return ResponseEntity.ok().body(Map.of(
                    "total", cart.getTotal(),
                    "itemCount", cart.getCartItems().size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cart/apply-coupon")
    @ResponseBody
    public ResponseEntity<?> applyCouponToCart(@RequestParam("cartId") Integer cartId,
                                               @RequestParam("couponCode") String couponCode,
                                               Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCartById(cartId);
            if (!cart.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Không có quyền áp dụng mã giảm giá"));
            }
            cartService.applyCoupon(cartId, couponCode);
            Cart updatedCart = cartService.getCartById(cartId);
            return ResponseEntity.ok().body(Map.of(
                    "message", "Áp dụng mã giảm giá thành công",
                    "subTotal", updatedCart.getSubTotal(),
                    "discountAmount", updatedCart.getDiscountAmount(),
                    "total", updatedCart.getTotal()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cart/remove-coupon")
    @ResponseBody
    public ResponseEntity<?> removeCouponFromCart(@RequestParam("cartId") Integer cartId,
                                                  Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCartById(cartId);
            if (!cart.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Không có quyền xóa mã giảm giá"));
            }
            cartService.removeCoupon(cartId);
            Cart updatedCart = cartService.getCartById(cartId);
            return ResponseEntity.ok().body(Map.of(
                    "message", "Xóa mã giảm giá thành công",
                    "subTotal", updatedCart.getSubTotal(),
                    "discountAmount", updatedCart.getDiscountAmount(),
                    "total", updatedCart.getTotal()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cart/clear")
    @ResponseBody
    public ResponseEntity<?> clearCart(Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            if (cart != null) {
                cartService.deleteCart(cart.getId());
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/wishlist")
    public String wishlist(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            model.addAttribute("cart", cart);
            model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
        }
        return "page/user/wishlist";
    }

    @PostMapping("/wishlist/toggle/{bookId}")
    @ResponseBody
    public ResponseEntity<?> toggleWishlist(@PathVariable("bookId") int bookId,
                                            Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            wishlistService.toggleWishlist(user.getId(), bookId);
            boolean isInWishlist = wishlistService.isBookInWishlist(user.getId(), bookId);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            return ResponseEntity.ok().body(Map.of(
                    "inWishlist", isInWishlist,
                    "wishlistCount", wishlists != null ? wishlists.size() : 0
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/wishlist/clear")
    @ResponseBody
    public ResponseEntity<?> clearWishlist(Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            wishlistService.clearWishlist(user.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

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

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            model.addAttribute("cart", cart);
            model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
            model.addAttribute("user", user);
        }
        return "page/user/profile";
    }

    @GetMapping("/settings")
    public String showSettings(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            model.addAttribute("cart", cart);
            model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
        }
        return "page/user/settings";
    }

    @PostMapping("/settings/update")
    public String updatePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng");
            return "redirect:/user/settings";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp");
            return "redirect:/user/settings";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);

        redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công");
        return "redirect:/user/settings";
    }

    @PostMapping("/profile/update-username")
    public String updateUsername(@RequestParam("newUsername") String newUsername,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByEmail(principal.getName());

            if (!passwordEncoder.matches(confirmPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu không đúng");
                return "redirect:/user/profile";
            }

            if (userService.existsByUsername(newUsername)) {
                redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại");
                return "redirect:/user/profile";
            }

            user.setUsername(newUsername);
            userService.save(user);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật tên đăng nhập thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/profile";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("birthOfDate") String birthOfDate,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam("addressLine") String addressLine,
            @RequestParam("ward") String ward,
            @RequestParam("district") String district,
            @RequestParam("city") String city,
            @RequestParam("country") String country,
            @RequestParam("postalCode") String postalCode,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getUserByEmail(principal.getName());

            if (!email.equals(currentUser.getEmail()) && userService.checkEmailExists(email)) {
                redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng");
                return "redirect:/user/profile";
            }

            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            currentUser.setEmail(email);
            currentUser.setPhoneNumber(phoneNumber);

            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                dateFormat.setLenient(false);
                Date date = dateFormat.parse(birthOfDate);
                if (date.after(new Date())) {
                    throw new IllegalArgumentException("Ngày sinh không thể là ngày trong tương lai");
                }
                currentUser.setBirthOfDate(date);
            } catch (ParseException e) {
                redirectAttributes.addFlashAttribute("error", "Định dạng ngày sinh không hợp lệ (dd/MM/yyyy)");
                return "redirect:/user/profile";
            }

            if (avatar != null && !avatar.isEmpty()) {
                String uploadDir = "src/main/resources/static/image/avatar";
                String oldAvatarPath = null;

                if (currentUser.getAvatar() != null) {
                    oldAvatarPath = "src/main/resources/static" + currentUser.getAvatar();
                }

                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String originalFilename = avatar.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String filename = UUID.randomUUID().toString() + extension;

                Path filePath = uploadPath.resolve(filename);
                Files.copy(avatar.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                currentUser.setAvatar("/image/avatar/" + filename);

                if (oldAvatarPath != null) {
                    try {
                        Files.deleteIfExists(Paths.get(oldAvatarPath));
                    } catch (IOException e) {
                        logger.warn("Không thể xóa avatar cũ: " + e.getMessage());
                    }
                }
            }

            if (currentUser.getAddresses() == null || currentUser.getAddresses().isEmpty()) {
                Address address = new Address();
                address.setUser(currentUser);
                address.setCreatedAt(new Date());
                currentUser.setAddresses(Arrays.asList(address));
            }
            Address address = currentUser.getAddresses().get(0);
            address.setAddressLine(addressLine);
            address.setWard(ward);
            address.setDistrict(district);
            address.setCity(city);
            address.setCountry(country);
            address.setPostalCode(postalCode);
            address.setCreatedAt(new Date());

            userService.save(currentUser);

            UserDetailsImpl userDetails = new UserDetailsImpl(currentUser, new ArrayList<>());
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            redirectAttributes.addFlashAttribute("success", "Thông tin cá nhân đã được cập nhật");

        } catch (IOException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/user/profile";
        }
        return "redirect:/user/profile";
    }

    @GetMapping("/purchase")
    public String showPurchaseForm(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        User user = userService.getUserByEmail(principal.getName());
        Cart cart = cartService.getCurrentCartByUser(user);
        List<Address> addresses = addressService.getAddressesByUserId(user.getId());
        List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);

        model.addAttribute("cart", cart);
        model.addAttribute("addresses", addresses);
        model.addAttribute("wishlists", wishlists);

        return "page/user/purchase";
    }

    @PostMapping("/purchase/confirm")
    public String confirmPurchase(
            @RequestParam("addressId") Integer addressId,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "couponCode", required = false) String couponCode,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);

            if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống!");
                return "redirect:/user/cart";
            }

            Address shippingAddress = addressService.getAddressById(addressId);
            if (shippingAddress == null || !Objects.equals(shippingAddress.getUser().getId(), user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Địa chỉ không hợp lệ!");
                return "redirect:/user/purchase";
            }

            Coupon coupon = null;
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                Optional<Coupon> couponOpt = couponService.findByCode(couponCode);
                if (couponOpt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Mã giảm giá không hợp lệ!");
                    return "redirect:/user/purchase";
                }
                coupon = couponOpt.get();
                BigDecimal subtotal = cart.getCartItems().stream()
                        .filter(item -> item.getUpdatedAt() == null)
                        .map(item -> new BigDecimal(item.getPrice()).multiply(new BigDecimal(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (!couponService.isValidForUse(coupon, subtotal)) {
                    redirectAttributes.addFlashAttribute("error", "Mã giảm giá không thể sử dụng!");
                    return "redirect:/user/purchase";
                }
            }

            OrderDetail order = orderDetailService.createOrder(user.getId(), addressId, paymentMethod, note, coupon);
            if (order != null) {
                if (coupon != null) {
                    couponService.useCoupon(coupon);
                }
                cartService.deleteCart(cart.getId());
                Cart newCart = new Cart();
                newCart.setUser(user);
                newCart.setCreatedAt(new Date());
                cartService.save(newCart);

                redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công!");
                return "redirect:/user/orders";
            } else {
                throw new RuntimeException("Không thể tạo đơn hàng!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/user/purchase";
        }
    }

    @GetMapping("/orders")
    public String showOrders(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        User user = userService.getUserByEmail(principal.getName());
        List<OrderDetail> orders = orderDetailService.getOrdersByUserId(user.getId());
        Cart cart = cartService.getCurrentCartByUser(user);
        List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);

        model.addAttribute("orders", orders);
        model.addAttribute("cart", cart);
        model.addAttribute("wishlists", wishlists);

        return "page/user/order";
    }

    @PostMapping("/order/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelOrder(@PathVariable Integer orderId, Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            OrderDetail order = orderDetailService.getOrderById(orderId);

            if (order == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Không tìm thấy đơn hàng!"));
            }

            if (!Objects.equals(order.getUser().getId(), user.getId())) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Không có quyền hủy đơn hàng này"));
            }

            if (!("PENDING".equals(order.getStatus()) || "CONFIRMED".equals(order.getStatus()))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Chỉ có thể hủy đơn hàng ở trạng thái chờ xử lý hoặc đã xác nhận"));
            }

            orderDetailService.cancelOrder(orderId);
            return ResponseEntity.ok()
                    .body(Map.of("message", "Hủy đơn hàng thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/order/{orderId}/pay")
    @ResponseBody
    public ResponseEntity<?> processPayment(@PathVariable Integer orderId, Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            OrderDetail order = orderDetailService.getOrderById(orderId);

            if (order == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Không tìm thấy đơn hàng!"));
            }

            if (!Objects.equals(order.getUser().getId(), user.getId())) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Không có quyền thanh toán đơn hàng này"));
            }

            PaymentDetail payment = order.getPayments().get(0);
            paymentDetailService.updatePaymentStatus(payment.getId(), "SUCCESS");

            return ResponseEntity.ok()
                    .body(Map.of("message", "Thanh toán thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}