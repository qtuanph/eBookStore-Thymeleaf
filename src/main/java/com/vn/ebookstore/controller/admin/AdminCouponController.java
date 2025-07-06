package com.vn.ebookstore.controller.admin;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vn.ebookstore.model.Coupon;
import com.vn.ebookstore.service.CouponService;

@Controller
@RequestMapping("/admin/coupons")
public class AdminCouponController {

    @Autowired
    private CouponService couponService;

    // Định dạng Date cho input type="datetime-local"
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(dateFormat, true));
    }

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
        model.addAttribute("search", search);
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        return "page/admin/coupons/list-coupons";
    }

    // Hiển thị form tạo mới
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("coupon", new Coupon());
        model.addAttribute("types", Arrays.asList("PERCENTAGE", "FIXED_AMOUNT"));
        model.addAttribute("formAction", "/admin/coupons/create");
        model.addAttribute("startDateFormatted", "");
        model.addAttribute("endDateFormatted", "");
        return "page/admin/coupons/form-coupons";
    }

    // Xử lý tạo mới
    @PostMapping("/create")
    public String createDiscount(@ModelAttribute Coupon coupon, Model model) {
        // Validate fields
        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            model.addAttribute("error", "Mã giảm giá không được để trống.");
        } else if (couponService.findByCode(coupon.getCode()).isPresent()) {
            model.addAttribute("error", "Mã giảm giá đã tồn tại.");
        } else if (coupon.getStartDate() == null || coupon.getEndDate() == null) {
            model.addAttribute("error", "Ngày bắt đầu và ngày kết thúc không được để trống.");
        } else if (coupon.getEndDate().before(coupon.getStartDate())) {
            model.addAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu.");
        } else {
            couponService.save(coupon);
            return "redirect:/admin/coupons?success=created";
        }

        // Trả lại form nếu có lỗi
        model.addAttribute("types", Arrays.asList("PERCENTAGE", "FIXED_AMOUNT"));
        model.addAttribute("formAction", "/admin/coupons/create");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        model.addAttribute("startDateFormatted", coupon.getStartDate() != null ? df.format(coupon.getStartDate()) : "");
        model.addAttribute("endDateFormatted", coupon.getEndDate() != null ? df.format(coupon.getEndDate()) : "");
        return "page/admin/coupons/form-coupons";
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
        model.addAttribute("formAction", "/admin/coupons/edit/" + id);

        // Format ngày cho input
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        model.addAttribute("startDateFormatted", coupon.getStartDate() != null ? df.format(coupon.getStartDate()) : "");
        model.addAttribute("endDateFormatted", coupon.getEndDate() != null ? df.format(coupon.getEndDate()) : "");
        return "page/admin/coupons/form-coupons";
    }

    // Xử lý cập nhật
    @PostMapping("/edit/{id}")
    public String updateCoupon(@PathVariable Integer id, @ModelAttribute Coupon coupon, Model model) {
        Coupon existingCoupon = couponService.findById(id).orElse(null);
        if (existingCoupon == null) {
            return "redirect:/admin/coupons?error=not-found";
        }
        // Validate fields
        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            model.addAttribute("error", "Mã giảm giá không được để trống.");
        } else if (!existingCoupon.getCode().equals(coupon.getCode()) && couponService.findByCode(coupon.getCode()).isPresent()) {
            model.addAttribute("error", "Mã giảm giá đã tồn tại.");
        } else if (coupon.getStartDate() == null || coupon.getEndDate() == null) {
            model.addAttribute("error", "Ngày bắt đầu và ngày kết thúc không được để trống.");
        } else if (coupon.getEndDate().before(coupon.getStartDate())) {
            model.addAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu.");
        } else {
            // Copy các thuộc tính
            existingCoupon.setCode(coupon.getCode());
            existingCoupon.setDescription(coupon.getDescription());
            existingCoupon.setDiscountType(coupon.getDiscountType());
            existingCoupon.setDiscountValue(coupon.getDiscountValue());
            existingCoupon.setMinPurchase(coupon.getMinPurchase());
            existingCoupon.setMaxDiscount(coupon.getMaxDiscount());
            existingCoupon.setStartDate(coupon.getStartDate());
            existingCoupon.setEndDate(coupon.getEndDate());
            existingCoupon.setUsageLimit(coupon.getUsageLimit());
            existingCoupon.setIsActive(coupon.getIsActive());
            couponService.save(existingCoupon);
            return "redirect:/admin/coupons?success=updated";
        }
        // Trả lại form nếu có lỗi
        model.addAttribute("coupon", coupon);
        model.addAttribute("types", Arrays.asList("PERCENTAGE", "FIXED_AMOUNT"));
        model.addAttribute("formAction", "/admin/coupons/edit/" + id);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        model.addAttribute("startDateFormatted", coupon.getStartDate() != null ? df.format(coupon.getStartDate()) : "");
        model.addAttribute("endDateFormatted", coupon.getEndDate() != null ? df.format(coupon.getEndDate()) : "");
        return "page/admin/coupons/form-coupons";
    }

    // Xử lý xóa
    @GetMapping("/delete/{id}")
    public String deleteCoupon(@PathVariable Integer id) {
        Coupon coupon = couponService.findById(id).orElse(null);
        if (coupon == null) {
            return "redirect:/admin/coupons?error=not-found";
        }
        if (!coupon.getOrders().isEmpty() || !coupon.getUserCoupons().isEmpty()) {
            return "redirect:/admin/coupons?error=coupon-in-use";
        }
        couponService.deleteCoupon(id);
        return "redirect:/admin/coupons?success=deleted";
    }
}
