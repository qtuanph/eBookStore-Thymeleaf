package com.vn.ebookstore.controller.guest;

import com.vn.ebookstore.model.Book;
import com.vn.ebookstore.model.Category;
import com.vn.ebookstore.service.BookService;
import com.vn.ebookstore.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;

@Controller
public class GuestController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BookService bookService;

    @GetMapping("/")
    public String home(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Book> premiumBooks = bookService.getPremiumBooks();
        List<Book> latestBooks = bookService.getLatestBooks();
        List<Book> bestSellers = bookService.getBestSellers();

        model.addAttribute("categories", categories);
        model.addAttribute("premiumBooks", premiumBooks);
        model.addAttribute("latestBooks", latestBooks);
        model.addAttribute("bestSellers", bestSellers);
        
        // Sửa đường dẫn mặc định cho ảnh bìa sách
        model.addAttribute("defaultCoverUrl", "/image/covers/default-cover.jpg");
        model.addAttribute("defaultAvatarUrl", "/image/avatar/default-avatar.jpg");
        model.addAttribute("wishlists", Collections.emptyList());
        model.addAttribute("cart", null);
        
        return "index";
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/about_us")
    public String aboutUs() {
        return "page/user/about_us";
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/products")
    public String products(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Book> books = bookService.getAllBooks();
        model.addAttribute("categories", categories);
        model.addAttribute("books", books);
        return "page/user/product";
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") int id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        return "page/user/product_detail";
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/faq")
    public String faq() {
        return "page/user/faq";
    }
}