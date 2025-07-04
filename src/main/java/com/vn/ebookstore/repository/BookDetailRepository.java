package com.vn.ebookstore.repository;

import com.vn.ebookstore.model.BookDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDetailRepository extends JpaRepository<BookDetail, Integer> {
    @Query("SELECT bd FROM BookDetail bd WHERE bd.book.id = :bookId")
    BookDetail findByBookId(@Param("bookId") Integer bookId);
}