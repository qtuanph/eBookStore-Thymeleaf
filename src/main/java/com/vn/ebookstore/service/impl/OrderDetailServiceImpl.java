package com.vn.ebookstore.service.impl;

import com.vn.ebookstore.model.*;
import com.vn.ebookstore.repository.*;
import com.vn.ebookstore.service.CouponService;
import com.vn.ebookstore.service.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CouponService couponService;

    @Autowired
    private PaymentDetailRepository paymentDetailRepository;

    @Override
    @Transactional
    public OrderDetail createOrder(Integer userId, Integer addressId, String paymentMethod, String note, Coupon coupon) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Cart cart = cartRepository.findByUserAndUpdatedAtIsNull(user)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            Address address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            OrderDetail order = new OrderDetail();
            order.setUser(user);
            order.setOrderAddress(formatAddress(address));
            order.setStatus("PENDING");
            order.setCreatedAt(new Date());

            BigDecimal subtotal = calculateSubtotal(cart.getCartItems());
            order.setSubTotal(subtotal);

            if (coupon != null) {
                if (!couponService.isValidForUse(coupon, subtotal)) {
                    throw new RuntimeException("Mã giảm giá không hợp lệ hoặc không thể sử dụng");
                }
                BigDecimal discount = couponService.calculateDiscount(coupon, subtotal);
                order.setCoupon(coupon);
                order.setDiscountAmount(discount);
                order.setTotal(subtotal.subtract(discount));
            } else {
                order.setTotal(subtotal);
            }

            List<OrderItem> orderItems = createOrderItems(cart.getCartItems(), order);
            order.setOrderItems(orderItems);

            PaymentDetail payment = createPaymentDetail(order, paymentMethod);
            order.setPayments(Arrays.asList(payment));

            order = orderDetailRepository.save(order);
            orderItemRepository.saveAll(orderItems);
            paymentDetailRepository.save(payment);

            return order;
        } catch (Exception e) {
            throw new RuntimeException("Error creating order: " + e.getMessage());
        }
    }

    private BigDecimal calculateSubtotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .filter(item -> item.getUpdatedAt() == null)
                .map(item -> new BigDecimal(item.getPrice()).multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private String formatAddress(Address address) {
        return String.format("%s, %s, %s, %s, %s, %s",
                address.getAddressLine(),
                address.getWard(),
                address.getDistrict(),
                address.getCity(),
                address.getCountry(),
                address.getPostalCode());
    }

    private List<OrderItem> createOrderItems(List<CartItem> cartItems, OrderDetail order) {
        return cartItems.stream()
                .filter(item -> item.getUpdatedAt() == null)
                .map(item -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setBook(item.getBook());
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setPrice(item.getPrice());
                    orderItem.setCreatedAt(new Date());
                    return orderItem;
                })
                .collect(Collectors.toList());
    }

    private PaymentDetail createPaymentDetail(OrderDetail order, String paymentMethod) {
        PaymentDetail payment = new PaymentDetail();
        payment.setOrder(order);
        payment.setAmount(order.getTotal());
        payment.setProvider(paymentMethod);
        payment.setStatus("PENDING");
        payment.setCreatedAt(new Date());
        return payment;
    }

    @Override
    public OrderDetail getOrderById(Integer id) {
        return orderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<OrderDetail> getOrdersByUserId(Integer userId) {
        return orderDetailRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void cancelOrder(Integer orderId) {
        OrderDetail order = getOrderById(orderId);
        if (!("PENDING".equals(order.getStatus()) || "CONFIRMED".equals(order.getStatus()))) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng ở trạng thái chờ xử lý hoặc đã xác nhận");
        }
        order.setStatus("CANCELLED");
        orderDetailRepository.save(order);
    }

    @Override
    @Transactional
    public OrderDetail updateOrderStatus(Integer orderId, String status) {
        OrderDetail order = getOrderById(orderId);
        order.setStatus(status);
        return orderDetailRepository.save(order);
    }

    @Override
    public List<OrderDetail> getAllOrders() {
        return orderDetailRepository.findAll();
    }

    @Override
    public OrderDetail save(OrderDetail order) {
        return orderDetailRepository.save(order);
    }

    @Override
    public BigDecimal getTotalRevenue() {
        return orderDetailRepository.findAll().stream()
                .filter(order -> "DELIVERED".equals(order.getStatus()))
                .map(OrderDetail::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public long countOrdersByStatus(String status) {
        return orderDetailRepository.countByStatus(status);
    }

    @Override
    public List<OrderDetail> getRecentOrders(PageRequest pageRequest) {
        return orderDetailRepository.findTopNByOrderByCreatedAtDesc(pageRequest);
    }

    @Override
    public long getTotalOrders() {
        return orderDetailRepository.count();
    }
}