package com.vn.ebookstore.service;

import com.vn.ebookstore.model.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

public interface CouponService {

    // Tìm coupon theo mã
    Optional<Coupon> findByCode(String code);

    // Tính toán số tiền giảm
    BigDecimal calculateDiscount(Coupon coupon, BigDecimal amount);

    // Kiểm tra coupon có hợp lệ để sử dụng không
    boolean isValidForUse(Coupon coupon, BigDecimal amount);

    // Sử dụng coupon (tăng số lần sử dụng)
    Coupon useCoupon(Coupon coupon);

    // Lấy tất cả coupon với phân trang
    Page<Coupon> getAllCoupons(Pageable pageable);

    // Lấy coupon theo loại và trạng thái
    Page<Coupon> getCouponsByTypeAndStatus(String type, String status, Pageable pageable);

    // Lấy trạng thái của coupon
    String getStatus(Coupon coupon);

    // Lưu coupon
    Coupon save(Coupon coupon);

    // Tìm coupon theo ID
    Optional<Coupon> findById(Integer id);

    // Xóa coupon
    void deleteCoupon(Integer id);

    // Tìm kiếm và lọc coupon
    Page<Coupon> searchCoupons(String search, String type, String status, Pageable pageable);
}