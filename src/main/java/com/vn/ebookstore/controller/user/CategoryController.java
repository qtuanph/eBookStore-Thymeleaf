package com.vn.ebookstore.controller.user;

import com.vn.ebookstore.model.*;
import com.vn.ebookstore.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private WishlistService wishlistService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private SubCategoryService subCategoryService; // Thêm service này

    @GetMapping("/user/category/{id}")
    public String showCategoryBooks(@PathVariable Integer id, Model model, Principal principal) {
        Category currentCategory = categoryService.getCategoryById(id);
        if (currentCategory == null) {
            return "redirect:/user/home";
        }

        List<Book> books = bookService.getBooksByCategory(id);
        List<Category> categories = categoryService.getAllCategories();
        
        model.addAttribute("currentCategory", currentCategory);
        model.addAttribute("categories", categories);
        model.addAttribute("books", books);
        model.addAttribute("isSubCategory", false);
        
        // Add default image paths
        model.addAttribute("defaultCoverUrl", "/image/covers/default-cover.jpg");
        model.addAttribute("defaultAvatarUrl", "/image/avatar/default-avatar.jpg");

        // Xử lý thông tin user nếu đã đăng nhập
        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            
            model.addAttribute("cart", cart);
            model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
        }

        return "page/user/category_books";
    }

    @GetMapping("/user/subcategory/{id}")
    public String showSubCategoryBooks(@PathVariable Integer id, Model model, Principal principal) {
        SubCategory subCategory = subCategoryService.getSubCategoryById(id);
        if (subCategory == null) {
            return "redirect:/user/home";
        }

        List<Book> books = bookService.getBooksBySubCategoryId(id);
        List<Category> categories = categoryService.getAllCategories();
        
        model.addAttribute("currentCategory", subCategory);
        model.addAttribute("parentCategory", subCategory.getCategory());
        model.addAttribute("categories", categories);
        model.addAttribute("books", books);
        model.addAttribute("isSubCategory", true);

        // Xử lý thông tin user nếu đã đăng nhập
        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            
            model.addAttribute("cart", cart);
            model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
        }

        return "page/user/category_books";
    }

    // Endpoint cho người dùng chưa đăng nhập
    @GetMapping("/category/{id}")
    public String showPublicCategoryBooks(@PathVariable Integer id, Model model) {
        List<Category> categories = categoryService.getAllCategories();
        Category currentCategory = categoryService.getCategoryById(id);
        List<Book> books;
        
        if (currentCategory.getSubCategories() != null && !currentCategory.getSubCategories().isEmpty()) {
            books = bookService.getBooksByCategory(id);
        } else {
            books = bookService.getBooksBySubCategoryId(id);
        }
        
        model.addAttribute("categories", categories);
        model.addAttribute("currentCategory", currentCategory);
        model.addAttribute("books", books);
        
        return "page/user/category_books";
    }

    @GetMapping("/subcategory/{id}")
    public String showPublicSubCategoryBooks(@PathVariable Integer id, Model model) {
        SubCategory subCategory = subCategoryService.getSubCategoryById(id);
        if (subCategory == null) {
            return "redirect:/home";
        }

        List<Book> books = bookService.getBooksBySubCategoryId(id);
        List<Category> categories = categoryService.getAllCategories();
        
        model.addAttribute("currentCategory", subCategory);
        model.addAttribute("parentCategory", subCategory.getCategory());
        model.addAttribute("categories", categories);
        model.addAttribute("books", books);
        model.addAttribute("isSubCategory", true);

        return "page/user/category_books";
    }
}
