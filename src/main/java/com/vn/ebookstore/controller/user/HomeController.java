package com.vn.ebookstore.controller.user;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
public class HomeController {
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
}
