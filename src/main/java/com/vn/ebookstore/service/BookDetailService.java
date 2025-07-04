package com.vn.ebookstore.service;

import com.vn.ebookstore.model.BookDetail;
import java.util.List;

public interface BookDetailService {
    BookDetail createBookDetail(BookDetail bookDetail);
    BookDetail updateBookDetail(int id, BookDetail bookDetail);
    void deleteBookDetail(int id);
    BookDetail getBookDetailById(int id);
    List<BookDetail> getAllBookDetails();
    BookDetail getBookDetailByBookId(int bookId);
}