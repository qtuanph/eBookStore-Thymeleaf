package com.vn.ebookstore.controller.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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

    private static final Path COVER_UPLOAD_DIR = Paths.get(System.getProperty("user.dir"), "uploads", "book");
    private static final Path PDF_UPLOAD_DIR = Paths.get(System.getProperty("user.dir"), "uploads", "pdf");

    private final BookService bookService;
    private final BookDetailService bookDetailService;
    private final SubCategoryService subCategoryService;
    private final CategoryService categoryService;

    public AdminBookController(BookService bookService, BookDetailService bookDetailService,
            SubCategoryService subCategoryService, CategoryService categoryService) {
        this.bookService = bookService;
        this.bookDetailService = bookDetailService;
        this.subCategoryService = subCategoryService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ModelAndView listBooks() {
        ModelAndView mav = new ModelAndView("page/admin/books/books");
        mav.addObject("books", bookService.getAllBooks());
        Book newBook = new Book();
        newBook.setBookDetail(new BookDetail());
        mav.addObject("book", newBook);
        mav.addObject("subCategories", subCategoryService.getAllSubCategories());
        return mav;
    }

    @GetMapping("/add")
    public ModelAndView showAddForm() {
        ModelAndView mav = new ModelAndView("page/admin/book-form");
        Book book = new Book();
        book.setBookDetail(new BookDetail());
        mav.addObject("book", book);
        mav.addObject("categories", categoryService.getAllCategories());
        mav.addObject("subCategories", subCategoryService.getAllSubCategories());
        return mav;
    }

    @PostMapping("/save")
    public String saveBook(@ModelAttribute("book") Book book,
            @RequestParam("coverFile") MultipartFile coverFile,
            @RequestParam("bookFile") MultipartFile bookFile) throws IOException {

        Files.createDirectories(COVER_UPLOAD_DIR);
        Files.createDirectories(PDF_UPLOAD_DIR);

        if (book.getBookDetail() == null) {
            book.setBookDetail(new BookDetail());
        }

        BookDetail detail = book.getBookDetail();
        detail.setBook(book);

        Book existingBook = book.getId() != null ? bookService.getBookById(book.getId()) : null;

        if (book.getSubCategory() != null && book.getSubCategory().getId() != null) {
            SubCategory subCategory = subCategoryService.getSubCategoryById(book.getSubCategory().getId());
            book.setSubCategory(subCategory);
        } else {
            book.setSubCategory(null);
        }

        if (!coverFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + coverFile.getOriginalFilename();
            Path savePath = COVER_UPLOAD_DIR.resolve(fileName);

            if (existingBook != null && existingBook.getCover() != null) {
                Path oldFile = COVER_UPLOAD_DIR.resolve(existingBook.getCover());
                Files.deleteIfExists(oldFile);
            }

            coverFile.transferTo(savePath);
            book.setCover(fileName);
        }

        if (!bookFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + bookFile.getOriginalFilename();
            Path savePath = PDF_UPLOAD_DIR.resolve(fileName);
            bookFile.transferTo(savePath);
            detail.setFileUrl(fileName);
        }

        if (existingBook != null) {
            BookDetail existingDetail = bookDetailService.getBookDetailByBookId(book.getId());
            if (existingDetail != null) {
                detail.setId(existingDetail.getId());
                bookDetailService.updateBookDetail(existingDetail.getId(), detail);
            } else {
                bookDetailService.createBookDetail(detail);
            }
            bookService.save(book);
        } else {
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
                    response.put("publicationDate",
                            detail.getPublicationDate() != null ? detail.getPublicationDate().toString() : null);
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

        Files.createDirectories(COVER_UPLOAD_DIR);
        Files.createDirectories(PDF_UPLOAD_DIR);

        Book existingBook = bookService.getBookById(book.getId());
        if (existingBook == null) {
            return "redirect:/admin/books?error=not-found";
        }

        // Cập nhật thông tin cơ bản
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPrice(book.getPrice());

        // Xử lý chi tiết sách
        BookDetail detail = existingBook.getBookDetail();
        if (detail == null) {
            detail = new BookDetail();
            detail.setBook(existingBook);
        }

        BookDetail incomingDetail = book.getBookDetail();
        if (incomingDetail != null) {
            detail.setDescription(incomingDetail.getDescription());
            detail.setSummary(incomingDetail.getSummary());
            detail.setIsbn(incomingDetail.getIsbn());
            detail.setPublisher(incomingDetail.getPublisher());
            detail.setPublicationDate(incomingDetail.getPublicationDate());
            detail.setPages(incomingDetail.getPages());
        }

        // ✅ Xử lý ảnh bìa (nếu có)
        if (coverFile != null && !coverFile.isEmpty()) {
            String oldCoverName = existingBook.getCover();
            if (oldCoverName != null && !oldCoverName.isBlank()) {
                Path oldCoverPath = COVER_UPLOAD_DIR.resolve(oldCoverName);
                if (Files.exists(oldCoverPath) && Files.isRegularFile(oldCoverPath)) {
                    Files.delete(oldCoverPath); // chỉ xóa file, không dính folder
                }
            }

            String newFileName = System.currentTimeMillis() + "_" + coverFile.getOriginalFilename();
            Path newCoverPath = COVER_UPLOAD_DIR.resolve(newFileName);
            coverFile.transferTo(newCoverPath);
            existingBook.setCover(newFileName);
        }

        // ✅ Xử lý file sách (nếu có)
        if (bookFile != null && !bookFile.isEmpty()) {
            String newPdfFileName = System.currentTimeMillis() + "_" + bookFile.getOriginalFilename();
            Path newPdfPath = PDF_UPLOAD_DIR.resolve(newPdfFileName);
            bookFile.transferTo(newPdfPath);
            detail.setFileUrl(newPdfFileName);
        }

        // Cập nhật SubCategory nếu có
        if (book.getSubCategory() != null && book.getSubCategory().getId() != null) {
            SubCategory subCategory = subCategoryService.getSubCategoryById(book.getSubCategory().getId());
            existingBook.setSubCategory(subCategory);
        }

        // Lưu mọi thứ
        bookService.save(existingBook);
        bookDetailService.updateBookDetail(detail.getId(), detail); // bạn nên đảm bảo ID detail tồn tại

        return "redirect:/admin/books?success=updated";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Integer id) {
        bookService.deleteBook(id);
        return "redirect:/admin/books";
    }
}
