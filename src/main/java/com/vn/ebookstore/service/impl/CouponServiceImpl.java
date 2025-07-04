package com.vn.ebookstore.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vn.ebookstore.model.Coupon;
import com.vn.ebookstore.repository.CouponRepository;
import com.vn.ebookstore.service.CouponService;

import jakarta.persistence.criteria.Predicate;

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

        try {
            if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
                BigDecimal percent = coupon.getDiscountValue()
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                BigDecimal discount = amount.multiply(percent);

                if (coupon.getMaxDiscount() != null) {
                    discount = discount.min(coupon.getMaxDiscount());
                }

                return discount.setScale(2, RoundingMode.HALF_UP);
            } else {
                return coupon.getDiscountValue().min(amount).setScale(2, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public boolean isValidForUse(Coupon coupon, BigDecimal amount) {
        if (coupon == null || !coupon.getIsActive()) {
            return false;
        }

        Date now = new Date();
        return now.after(coupon.getStartDate())
                && now.before(coupon.getEndDate())
                && (coupon.getUsageLimit() == null || coupon.getTimesUsed() < coupon.getUsageLimit())
                && (coupon.getMinPurchase() == null || amount.compareTo(coupon.getMinPurchase()) >= 0);
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
                return getAllCoupons(pageable);
            }
        }
        return getAllCoupons(pageable);
    }

    @Override
    public String getStatus(Coupon coupon) {
        Date now = new Date();
        if (!coupon.getIsActive()) return "inactive";
        if (now.before(coupon.getStartDate())) return "not_started";
        if (now.after(coupon.getEndDate())) return "expired";

        long diffInDays = (coupon.getEndDate().getTime() - now.getTime()) / (24 * 60 * 60 * 1000);
        return (diffInDays <= 7) ? "soon_to_expire" : "active";
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
        Specification<Coupon> spec = (root, _, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                Predicate codePredicate = cb.like(cb.lower(root.get("code")), "%" + search.toLowerCase() + "%");
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%");
                predicates.add(cb.or(codePredicate, descPredicate));
            }

            if (type != null && !type.trim().isEmpty()) {
                try {
                    Coupon.DiscountType enumType = Coupon.DiscountType.valueOf(type);
                    predicates.add(cb.equal(root.get("discountType"), enumType));
                } catch (IllegalArgumentException e) {
                    // ignore invalid type
                }
            }

            if (status != null && !status.trim().isEmpty()) {
                Date now = new Date();
                switch (status) {
                    case "active" -> {
                        predicates.add(cb.equal(root.get("isActive"), true));
                        predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), now));
                        predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), now));
                    }
                    case "expired" -> {
                        predicates.add(cb.equal(root.get("isActive"), true));
                        predicates.add(cb.lessThan(root.get("endDate"), now));
                    }
                    case "inactive" -> predicates.add(cb.equal(root.get("isActive"), false));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };

        return couponRepository.findAll(spec, pageable);
    }
}
