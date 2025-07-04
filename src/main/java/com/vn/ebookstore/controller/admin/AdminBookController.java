package com.vn.ebookstore.controller.admin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.vn.ebookstore.model.Book;
import com.vn.ebookstore.model.BookDetail;
import com.vn.ebookstore.model.SubCategory;
import com.vn.ebookstore.service.BookDetailService;
import com.vn.ebookstore.service.BookService;
import com.vn.ebookstore.service.CategoryService;
import com.vn.ebookstore.service.SubCategoryService;


@Controller
@RequestMapping("/admin/books")
public class AdminBookController {

    private final BookService bookService;
    private final BookDetailService bookDetailService;
    private final SubCategoryService subCategoryService;
    private final CategoryService categoryService;

    public AdminBookController(BookService bookService, BookDetailService bookDetailService, SubCategoryService subCategoryService, CategoryService categoryService) {
        this.bookService = bookService;
        this.bookDetailService = bookDetailService;
        this.subCategoryService = subCategoryService;
        this.categoryService = categoryService;
    }


    @GetMapping
    public ModelAndView listBooks() {
        ModelAndView mav = new ModelAndView("page/admin/books/books");
        mav.addObject("books", bookService.getAllBooks());
        
        // Thêm đối tượng book mới cho form
        Book newBook = new Book();
        newBook.setBookDetail(new BookDetail());
        mav.addObject("book", newBook);
        
        // Thêm danh sách subcategories cho dropdown
        mav.addObject("subCategories", subCategoryService.getAllSubCategories());
        
        return mav;
    }

    @GetMapping("/add")
    public ModelAndView showAddForm() {
        ModelAndView mav = new ModelAndView("page/admin/book-form");
        Book book = new Book();
        book.setBookDetail(new BookDetail()); // tạo sẵn để binding form tránh null pointer
        
        mav.addObject("book", book);
        mav.addObject("categories", categoryService.getAllCategories());
        mav.addObject("subCategories", subCategoryService.getAllSubCategories()); // cần có service này
        
        return mav;
    }

@PostMapping("/save")
public String saveBook(@ModelAttribute("book") Book book,
                       @RequestParam("coverFile") MultipartFile coverFile,
                       @RequestParam("bookFile") MultipartFile bookFile) throws IOException {

    // Tạo mới BookDetail nếu chưa có
    if (book.getBookDetail() == null) {
        book.setBookDetail(new BookDetail());
    }

    // Gán quan hệ 2 chiều
    BookDetail detail = book.getBookDetail();
    detail.setBook(book);

    // Kiểm tra xem đã tồn tại Book theo ID chưa
    Book existingBook = null;
    if (book.getId() != null) {
        existingBook = bookService.getBookById(book.getId());
    }

    // Xử lý subCategory: load từ DB nếu có, nếu không thì set null
    if (book.getSubCategory() != null || book.getSubCategory().getId() != null) {
        SubCategory subCategory = subCategoryService.getSubCategoryById(book.getSubCategory().getId());
        book.setSubCategory(subCategory);
    } else {
        book.setSubCategory(null);
    }

    // Upload ảnh bìa
    if (!coverFile.isEmpty()) {
        String coverFileName = coverFile.getOriginalFilename();
        String coverUploadPath = "E:/suasang/eBookStore-Thymeleaf/uploads/book";
        File coverSaveFile = new File(coverUploadPath, coverFileName);
        coverFile.transferTo(coverSaveFile);
        book.setCover(coverFileName);
    }

    // Upload file PDF
    if (!bookFile.isEmpty()) {
        String bookFileName = bookFile.getOriginalFilename();
        String bookUploadPath = "E:/suasang/eBookStore-Thymeleaf/uploads/pdf";
        File bookSaveFile = new File(bookUploadPath, bookFileName);
        bookFile.transferTo(bookSaveFile);
        detail.setFileUrl(bookFileName);
    }

    if (existingBook != null) {
        // Đã có sách trong DB, cập nhật BookDetail
        BookDetail existingDetail = bookDetailService.getBookDetailByBookId(book.getId());
        if (existingDetail != null) {
            detail.setId(existingDetail.getId());
            bookDetailService.updateBookDetail(existingDetail.getId(), detail);
        } else {
            bookDetailService.createBookDetail(detail);
        }
        // Cập nhật sách
        bookService.save(book);
    } else {
        // Sách mới, lưu luôn
        bookService.save(book);
    }

    return "redirect:/admin/books";
}

    @GetMapping("/edit/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBookForEdit(@PathVariable Integer id) {
        try {
            Book book = bookService.getBookById(id);
            if (book != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", book.getId());
                response.put("title", book.getTitle());
                response.put("author", book.getAuthor());
                response.put("price", book.getPrice());
                response.put("cover", book.getCover());
                
                if (book.getSubCategory() != null) {
                    response.put("subCategoryId", book.getSubCategory().getId());
                }
                
                if (book.getBookDetail() != null) {
                    BookDetail detail = book.getBookDetail();
                    response.put("description", detail.getDescription());
                    response.put("summary", detail.getSummary());
                    response.put("isbn", detail.getIsbn());
                    response.put("publisher", detail.getPublisher());
                    response.put("publicationDate", detail.getPublicationDate() != null ? 
                        detail.getPublicationDate().toString() : null);
                    response.put("pages", detail.getPages());
                    response.put("fileUrl", detail.getFileUrl());
                }
                
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

@PostMapping("/update")
public String updateBook(@ModelAttribute Book book,
                        @RequestParam(required = false) MultipartFile coverFile,
                        @RequestParam(required = false) MultipartFile bookFile) throws IOException {
    try {
        // Lấy book hiện tại từ DB
        Book existingBook = bookService.getBookById(book.getId());
        if (existingBook == null) {
            return "redirect:/admin/books?error=not-found";
        }

        // Cập nhật thông tin cơ bản
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPrice(book.getPrice());

        // Cập nhật BookDetail
        BookDetail detail = existingBook.getBookDetail();
        if (detail == null) {
            detail = new BookDetail();
            detail.setBook(existingBook);
        }
        detail.setDescription(book.getBookDetail().getDescription());
        detail.setSummary(book.getBookDetail().getSummary());
        detail.setIsbn(book.getBookDetail().getIsbn());
        detail.setPublisher(book.getBookDetail().getPublisher());
        detail.setPublicationDate(book.getBookDetail().getPublicationDate());
        detail.setPages(book.getBookDetail().getPages());

        // Xử lý upload ảnh mới nếu có
        if (coverFile != null && !coverFile.isEmpty()) {
            String coverFileName = coverFile.getOriginalFilename();
            String coverUploadPath = new ClassPathResource("static/image/cover").getFile().getAbsolutePath();
            File coverSaveFile = new File(coverUploadPath, coverFileName);
            coverFile.transferTo(coverSaveFile);
            existingBook.setCover(coverFileName);
        }

        // Xử lý upload PDF mới nếu có
        if (bookFile != null && !bookFile.isEmpty()) {
            String bookFileName = bookFile.getOriginalFilename();
            String bookUploadPath = new ClassPathResource("static/image").getFile().getAbsolutePath();
            File bookSaveFile = new File(bookUploadPath, bookFileName);
            bookFile.transferTo(bookSaveFile);
            detail.setFileUrl(bookFileName);
        }

        // Cập nhật SubCategory
        if (book.getSubCategory() != null && book.getSubCategory().getId() != null) {
            SubCategory subCategory = subCategoryService.getSubCategoryById(book.getSubCategory().getId());
            existingBook.setSubCategory(subCategory);
        }

        // Lưu vào DB
        bookService.save(existingBook);
        return "redirect:/admin/books?success=updated";
    } catch (IOException | IllegalStateException e) {
        return "redirect:/admin/books?error=update-failed";
    }
}

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Integer id) {
        bookService.deleteBook(id);
        return "redirect:/admin/books";
    }
}