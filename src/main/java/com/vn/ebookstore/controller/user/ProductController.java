package com.vn.ebookstore.controller.user;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vn.ebookstore.model.Book;
import com.vn.ebookstore.model.Cart;
import com.vn.ebookstore.model.Category;
import com.vn.ebookstore.model.User;
import com.vn.ebookstore.model.Wishlist;
import com.vn.ebookstore.service.BookService;
import com.vn.ebookstore.service.CartService;
import com.vn.ebookstore.service.CategoryService;
import com.vn.ebookstore.service.UserService;
import com.vn.ebookstore.service.WishlistService;

@Controller
@RequestMapping("/user")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BookService bookService;
    @Autowired
    private UserService userService;
    @Autowired
    private CartService cartService;
    @Autowired
    private WishlistService wishlistService;

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
}
