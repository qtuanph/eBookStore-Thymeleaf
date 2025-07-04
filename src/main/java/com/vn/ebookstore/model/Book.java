package com.vn.ebookstore.model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String author;
    private String cover;
    private Long price;
    
    @Column(name = "created_at", updatable = false)
    private Date createdAt;
    
    @Column(name = "deleted_at")
    private Date deletedAt;

    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;

    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private BookDetail bookDetail;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookImage> images;

    @OneToMany(mappedBy = "book")
    private List<Wishlist> wishlists;

    @OneToMany(mappedBy = "book")
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "book")
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @Transient
    private Double averageRating;

    @Transient
    private Integer reviewCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public BookDetail getBookDetail() {
        return bookDetail;
    }

    public void setBookDetail(BookDetail bookDetail) {
        this.bookDetail = bookDetail;
    }

    public List<BookImage> getImages() {
        return images;
    }

    public void setImages(List<BookImage> images) {
        this.images = images;
    }

    public List<Wishlist> getWishlists() {
        return wishlists;
    }

    public void setWishlists(List<Wishlist> wishlists) {
        this.wishlists = wishlists;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public Double getAverageRating() {
        if (this.reviews != null && !this.reviews.isEmpty()) {
            double sum = this.reviews.stream()
                    .mapToInt(Review::getRating)
                    .sum();
            // Làm tròn đến 1 chữ số thập phân
            return Math.round((sum / this.reviews.size()) * 10.0) / 10.0;
        }
        return 0.0;
    }

    public Integer getReviewCount() {
        return this.reviews != null ? this.reviews.size() : 0;
    }

    public void updateAverageRating() {
        if (this.reviews != null && !this.reviews.isEmpty()) {
            double sum = 0;
            for (Review review : this.reviews) {
                sum += review.getRating();
            }
            this.averageRating = sum / this.reviews.size();
            this.reviewCount = this.reviews.size();
        } else {
            this.averageRating = null;
            this.reviewCount = 0;
        }
    }
}