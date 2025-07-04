package com.vn.ebookstore.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vn.ebookstore.model.Book;
import com.vn.ebookstore.model.Category;
import com.vn.ebookstore.repository.BookRepository;
import com.vn.ebookstore.repository.CategoryRepository;
import com.vn.ebookstore.repository.SubCategoryRepository;
import com.vn.ebookstore.service.BookService;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    
    @Override
    public void save(Book book) {
         bookRepository.save(book);
    }

    @Override
    public Book createBook(Book book) {
         // Nếu bookDetail tồn tại, cần thiết lập lại quan hệ 2 chiều
        if (book.getBookDetail() != null) {
        book.getBookDetail().setBook(book); // rất quan trọng
        }

        return bookRepository.save(book); // cascade sẽ lưu luôn bookDetail
    }

    @Override
    public Book updateBook(int id, Book book) {
        Book existingBook = bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setCover(book.getCover());
        existingBook.setSubCategory(book.getSubCategory());
        existingBook.setPrice(book.getPrice());
        return bookRepository.save(existingBook);
    }

    @Override
    public void deleteBook(int id) {
        bookRepository.deleteById(id);
    }

    @Override
    public Book getBookById(int id) {
        return bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
    }

    @Override
    public Optional<Book> findById(Integer id) {
        return bookRepository.findById(id);
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public List<Book> getBooksByCategory(int categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if (category.getDeletedAt() != null) {
            throw new RuntimeException("Category has been deleted");
        }

          List<Integer> subCategoryIds = category.getSubCategories()
            .stream()
            .filter(sub -> sub.getDeletedAt() == null)
            .map(sub -> sub.getId())
            .toList();

        if (subCategoryIds.isEmpty()) return List.of(); // Không có sub nào

        return bookRepository.findBySubCategory_Category_IdAndDeletedAtIsNull(categoryId);
    }

    @Override
    public List<Book> getLatestBooks() {
        return bookRepository.findLatestBooks();
    }

    @Override
    public List<Book> getPremiumBooks() {
        return bookRepository.findPremiumBooks();
    }

    @Override
    public void softDeleteBook(int id) {
        Book book = getBookById(id);
        book.setDeletedAt(new Date());
        bookRepository.save(book);
    }

    @Override 
    public List<Book> getBooksBySubCategoryId(Integer subCategoryId) {
        if (!subCategoryRepository.existsById(subCategoryId)) {
            throw new RuntimeException("SubCategory not found");
        }
        return bookRepository.findBySubCategoryIdAndDeletedAtIsNull(subCategoryId);
    }

    @Override
    public List<Book> searchBooks(String query) {
        return bookRepository.findByTitleContainingIgnoreCase(query);
    }

    @Override
    public List<Book> getBestSellers() {
        return bookRepository.findBestSellingBooks();
    }

    @Override
    public List<Book> filterAndSortBooks(
            Integer categoryId,
            Integer subCategoryId,
            Double minPrice,
            Double maxPrice, 
            String sortBy,
            String sortDirection,
            Float minRating) {
        
        List<Book> books = bookRepository.filterAndSortBooks(
            categoryId, 
            subCategoryId,
            minPrice, 
            maxPrice,
            sortBy,
            sortDirection,
            minRating
        );

        // Nếu có yêu cầu lọc theo rating
        if (minRating != null) {
            List<Book> booksWithRating = bookRepository.findByAverageRatingGreaterThanEqual(minRating);
            books.retainAll(booksWithRating); // Chỉ giữ lại các sách thỏa mãn cả hai điều kiện
        }

        return books;
    }

    @Override
    public Double getLowestPrice() {
        return bookRepository.findLowestPrice();
    }

    @Override
    public Double getHighestPrice() {
        return bookRepository.findHighestPrice();
    }

    @Override
    public long getTotalBooks() {
        return bookRepository.countByDeletedAtIsNull();
    }
}