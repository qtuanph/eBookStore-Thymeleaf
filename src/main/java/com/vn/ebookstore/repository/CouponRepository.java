package com.vn.ebookstore.repository;

import com.vn.ebookstore.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer>, JpaSpecificationExecutor<Coupon> {
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);
    boolean existsByCode(String code);

    // Sửa tham số từ String thành enum
    @Query("SELECT c FROM Coupon c WHERE c.discountType = :discountType")
    Page<Coupon> findByDiscountType(@Param("discountType") Coupon.DiscountType discountType, Pageable pageable);

    // Sửa truy vấn cho các trạng thái
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND CURRENT_TIMESTAMP BETWEEN c.startDate AND c.endDate")
    Page<Coupon> findActiveCoupons(Pageable pageable);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.endDate < CURRENT_TIMESTAMP")
    Page<Coupon> findExpiredCoupons(Pageable pageable);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = false")
    Page<Coupon> findInactiveCoupons(Pageable pageable);
}