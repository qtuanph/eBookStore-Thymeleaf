package com.vn.ebookstore.repository;

import com.vn.ebookstore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    List<Book> findBySubCategory_Category_IdAndDeletedAtIsNull(Integer categoryId);
    List<Book> findBySubCategoryIdAndDeletedAtIsNull(Integer subCategoryId);
    List<Book> findBySubCategoryIdInAndDeletedAtIsNull(List<Integer> subCategoryIds);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.bookDetail WHERE b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<Book> findAllWithBookDetail();


    @Query("SELECT b FROM Book b WHERE b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<Book> findLatestBooks();
    
    @Query("SELECT b FROM Book b WHERE b.deletedAt IS NULL ORDER BY b.price DESC")
    List<Book> findPremiumBooks();
    
    List<Book> findByTitleContainingIgnoreCase(String query); // Method tìm kiếm theo title
    
    // Thêm method để lấy sách bán chạy (ví dụ: dựa vào số lượng đơn hàng)
    @Query("SELECT b FROM Book b JOIN b.orderItems oi " +
           "GROUP BY b " +
           "ORDER BY COUNT(oi) DESC")
    List<Book> findBestSellingBooks();
    
    @Query("SELECT DISTINCT b FROM Book b " +
           "LEFT JOIN b.reviews r " +
           "WHERE (:categoryId IS NULL OR b.subCategory.category.id = :categoryId) " +
           "AND (:subCategoryId IS NULL OR b.subCategory.id = :subCategoryId) " +
           "AND (CAST(:minPrice AS double) IS NULL OR b.price >= :minPrice) " + 
           "AND (CAST(:maxPrice AS double) IS NULL OR b.price <= :maxPrice) " +
           "AND b.deletedAt IS NULL " +
           "GROUP BY b " +
           "HAVING (:minRating IS NULL OR COALESCE(AVG(r.rating), 0) >= :minRating) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'price' AND :sortDir = 'asc' THEN b.price END ASC, " +
           "CASE WHEN :sortBy = 'price' AND :sortDir = 'desc' THEN b.price END DESC, " +
           "CASE WHEN :sortBy = 'rating' AND :sortDir = 'desc' THEN AVG(r.rating) END DESC, " +
           "CASE WHEN :sortBy = 'newest' THEN b.createdAt END DESC")
    List<Book> filterAndSortBooks(
        @Param("categoryId") Integer categoryId,
        @Param("subCategoryId") Integer subCategoryId, 
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("sortBy") String sortBy,
        @Param("sortDir") String sortDir,
        @Param("minRating") Float minRating
    );

    @Query("SELECT MIN(b.price) FROM Book b WHERE b.deletedAt IS NULL")
    Double findLowestPrice();

    @Query("SELECT MAX(b.price) FROM Book b WHERE b.deletedAt IS NULL") 
    Double findHighestPrice();
    
    @Query("SELECT b FROM Book b " +
           "JOIN Review r ON b = r.book " +
           "GROUP BY b " +
           "HAVING AVG(r.rating) >= :minRating")
    List<Book> findByAverageRatingGreaterThanEqual(@Param("minRating") Float minRating);

    long countByDeletedAtIsNull();
}