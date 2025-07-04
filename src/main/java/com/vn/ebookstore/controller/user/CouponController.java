package com.vn.ebookstore.controller.user;

import com.vn.ebookstore.model.Coupon;
import com.vn.ebookstore.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            Double amount = Double.valueOf(payload.get("amount").toString());
            
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

            Double discount = couponService.calculateDiscount(coupon, amount);
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "discount", discount,
                "message", "Mã giảm giá hợp lệ"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "message", e.getMessage()
            ));
        }
    }
}
