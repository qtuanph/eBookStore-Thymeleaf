package com.vn.ebookstore.controller.admin;

import com.vn.ebookstore.model.Coupon;
import com.vn.ebookstore.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Controller
@RequestMapping("/admin/coupons") // Sửa từ "/admin/coupons" thành "/admin/coupons"
public class AdminCouponController {

    @Autowired
    private CouponService couponService;

    // Hiển thị danh sách mã giảm giá
    @GetMapping("")
    public String listDiscounts(Model model,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "search", required = false) String search,
                                @RequestParam(value = "type", required = false) String type,
                                @RequestParam(value = "status", required = false) String status) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("id").descending());
        Page<Coupon> coupons = couponService.searchCoupons(search, type, status, pageable);
        model.addAttribute("coupons", coupons);
        return "page/admin/coupons/list-coupons";
    }

    // Hiển thị form tạo mới
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("coupon", new Coupon());
        model.addAttribute("types", Arrays.asList("PERCENTAGE", "FIXED_AMOUNT"));
        return "page/admin/coupons/create-coupons";
    }

    // Xử lý tạo mới
    @PostMapping("/create")
    public String createDiscount(@ModelAttribute Coupon coupon) {
        try {
            couponService.save(coupon);
            return "redirect:/admin/coupons?success=created";
        } catch (Exception e) {
            return "redirect:/admin/coupons?error=" + e.getMessage();
        }
    }

    // Hiển thị form sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Coupon coupon = couponService.findById(id).orElse(null);
        if (coupon == null) {
            return "redirect:/admin/coupons?error=not-found";
        }
        model.addAttribute("coupon", coupon);
        model.addAttribute("types", Arrays.asList("PERCENTAGE", "FIXED_AMOUNT"));
        return "page/admin/coupons/form-discount";
    }

    // Xử lý cập nhật
    @PostMapping("/edit/{id}")
    public String updateCoupon(@PathVariable Integer id, @ModelAttribute Coupon coupon) {
        try {
            Coupon existingCoupon = couponService.findById(id).orElse(null);
            if (existingCoupon == null) {
                return "redirect:/admin/coupons?error=not-found";
            }
            coupon.setId(id); // Đảm bảo ID được set
            couponService.save(coupon);
            return "redirect:/admin/coupons?success=updated";
        } catch (Exception e) {
            return "redirect:/admin/coupons?error=" + e.getMessage();
        }
    }

    // Xử lý xóa
    @GetMapping("/delete/{id}")
    public String deleteCoupon(@PathVariable Integer id) {
        try {
            Coupon coupon = couponService.findById(id).orElse(null);
            if (coupon != null) {
                couponService.deleteCoupon(id);
                return "redirect:/admin/coupons?success=deleted";
            } else {
                return "redirect:/admin/coupons?error=not-found";
            }
        } catch (Exception e) {
            return "redirect:/admin/coupons?error=" + e.getMessage();
        }
    }
}