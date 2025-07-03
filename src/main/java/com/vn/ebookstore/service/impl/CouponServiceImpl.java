package com.vn.ebookstore.service.impl;

import com.vn.ebookstore.model.Coupon;
import com.vn.ebookstore.repository.CouponRepository;
import com.vn.ebookstore.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Override
    public Optional<Coupon> findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        return couponRepository.findByCodeAndIsActiveTrue(code.toUpperCase());
    }

    @Override
    @Transactional
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal amount) {
        if (!isValidForUse(coupon, amount)) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;
        try {
            if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
                discount = amount.multiply(coupon.getDiscountValue().divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP));
                if (coupon.getMaxDiscount() != null) {
                    discount = discount.min(coupon.getMaxDiscount());
                }
            } else {
                discount = coupon.getDiscountValue().min(amount);
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }

        return discount.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public boolean isValidForUse(Coupon coupon, BigDecimal amount) {
        if (coupon == null || !coupon.getIsActive()) {
            return false;
        }

        Date now = new Date();
        return coupon.getIsActive() &&
                now.after(coupon.getStartDate()) &&
                now.before(coupon.getEndDate()) &&
                (coupon.getUsageLimit() == null || coupon.getTimesUsed() < coupon.getUsageLimit()) &&
                (coupon.getMinPurchase() == null || amount.compareTo(coupon.getMinPurchase()) >= 0);
    }

    @Override
    @Transactional
    public Coupon useCoupon(Coupon coupon) {
        if (coupon == null) {
            throw new IllegalArgumentException("Coupon cannot be null");
        }

        if (!isValidForUse(coupon, BigDecimal.ZERO)) {
            throw new RuntimeException("Coupon is no longer valid");
        }

        coupon.setTimesUsed(coupon.getTimesUsed() + 1);

        if (coupon.getUsageLimit() != null && coupon.getTimesUsed() >= coupon.getUsageLimit()) {
            coupon.setIsActive(false);
        }

        return couponRepository.save(coupon);
    }

    @Override
    public Page<Coupon> getAllCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable);
    }

    @Override
    public Page<Coupon> getCouponsByTypeAndStatus(String type, String status, Pageable pageable) {
        Date now = new Date();
        if ("active".equals(status)) {
            return couponRepository.findActiveCoupons(pageable);
        } else if ("expired".equals(status)) {
            return couponRepository.findExpiredCoupons(pageable);
        } else if ("inactive".equals(status)) {
            return couponRepository.findInactiveCoupons(pageable);
        } else if (type != null && !type.trim().isEmpty()) {
            try {
                Coupon.DiscountType enumType = Coupon.DiscountType.valueOf(type);
                return couponRepository.findByDiscountType(enumType, pageable);
            } catch (IllegalArgumentException e) {
                // handle invalid type string (ví dụ: return empty page hoặc default findAll)
                return getAllCoupons(pageable);
            }
        }
        return getAllCoupons(pageable);
    }

    @Override
    public String getStatus(Coupon coupon) {
        Date now = new Date();
        if (!coupon.getIsActive()) {
            return "inactive";
        }
        if (now.before(coupon.getStartDate())) {
            return "not_started";
        }
        if (now.after(coupon.getEndDate())) {
            return "expired";
        }
        long diffInMillies = Math.abs(coupon.getEndDate().getTime() - now.getTime());
        long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);
        if (diffInDays <= 7) {
            return "soon_to_expire";
        }
        return "active";
    }

    @Override
    @Transactional
    public Coupon save(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Override
    public Optional<Coupon> findById(Integer id) {
        return couponRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteCoupon(Integer id) {
        couponRepository.deleteById(id);
    }

    @Override
    public Page<Coupon> searchCoupons(String search, String type, String status, Pageable pageable) {
        Specification<Coupon> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tìm kiếm theo code hoặc description
            if (search != null && !search.trim().isEmpty()) {
                Predicate codePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("code")),
                        "%" + search.toLowerCase() + "%"
                );
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        "%" + search.toLowerCase() + "%"
                );
                predicates.add(criteriaBuilder.or(codePredicate, descriptionPredicate));
            }

            // Lọc theo loại
            if (type != null && !type.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("discountType"), Coupon.DiscountType.valueOf(type)));
            }

            // Lọc theo trạng thái
            if (status != null && !status.trim().isEmpty()) {
                Date now = new Date();
                switch (status) {
                    case "active":
                        predicates.add(criteriaBuilder.equal(root.get("isActive"), true));
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), now));
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), now));
                        break;
                    case "expired":
                        predicates.add(criteriaBuilder.equal(root.get("isActive"), true));
                        predicates.add(criteriaBuilder.lessThan(root.get("endDate"), now));
                        break;
                    case "inactive":
                        predicates.add(criteriaBuilder.equal(root.get("isActive"), false));
                        break;
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return couponRepository.findAll(spec, pageable);
    }
}