package com.vn.ebookstore.service.impl;

import com.vn.ebookstore.model.BookDetail;
import com.vn.ebookstore.repository.BookDetailRepository;
import com.vn.ebookstore.service.BookDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookDetailServiceImpl implements BookDetailService {

    @Autowired
    private BookDetailRepository bookDetailRepository;

    @Override
    public BookDetail createBookDetail(BookDetail bookDetail) {
        return bookDetailRepository.save(bookDetail);
    }

    @Override
    public BookDetail updateBookDetail(int id, BookDetail bookDetail) {
        BookDetail existingDetail = bookDetailRepository.findById(id).orElseThrow(() -> new RuntimeException("BookDetail not found"));
        existingDetail.setDescription(bookDetail.getDescription());
        existingDetail.setSummary(bookDetail.getSummary());
        existingDetail.setIsbn(bookDetail.getIsbn());
        existingDetail.setPublisher(bookDetail.getPublisher());
        existingDetail.setPublicationDate(bookDetail.getPublicationDate());
        existingDetail.setPages(bookDetail.getPages());
        existingDetail.setFileUrl(bookDetail.getFileUrl());
        return bookDetailRepository.save(existingDetail);
    }
    @Override
    public BookDetail getBookDetailByBookId(int bookId) {
        return bookDetailRepository.findByBookId(bookId);
    }

    @Override
    public void deleteBookDetail(int id) {
        bookDetailRepository.deleteById(id);
    }

    @Override
    public BookDetail getBookDetailById(int id) {
        return bookDetailRepository.findById(id).orElseThrow(() -> new RuntimeException("BookDetail not found"));
    }

    @Override
    public List<BookDetail> getAllBookDetails() {
        return bookDetailRepository.findAll();
    }
}