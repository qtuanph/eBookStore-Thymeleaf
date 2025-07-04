package com.vn.ebookstore.controller.user;

import com.vn.ebookstore.model.Coupon;
import com.vn.ebookstore.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @PostMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestBody Map<String, Object> payload) {
        try {
            String code = (String) payload.get("code");
            String amountStr = payload.get("amount") != null ? payload.get("amount").toString() : null;
            if (code == null || amountStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "valid", false,
                        "message", "Mã giảm giá và số tiền không được để trống"
                ));
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr).setScale(2, BigDecimal.ROUND_HALF_UP);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "valid", false,
                        "message", "Số tiền không hợp lệ"
                ));
            }

            Optional<Coupon> couponOpt = couponService.findByCode(code);
            if (couponOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Mã giảm giá không hợp lệ"
                ));
            }

            Coupon coupon = couponOpt.get();
            if (!couponService.isValidForUse(coupon, amount)) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Mã giảm giá không thể sử dụng"
                ));
            }

            BigDecimal discount = couponService.calculateDiscount(coupon, amount);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "discount", discount,
                    "message", "Mã giảm giá hợp lệ"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", "Lỗi: " + e.getMessage()
            ));
        }
    }
}