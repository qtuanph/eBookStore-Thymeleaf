package com.vn.ebookstore.service.impl;

import com.vn.ebookstore.model.PaymentDetail;
import com.vn.ebookstore.model.OrderDetail;
import com.vn.ebookstore.repository.PaymentDetailRepository;
import com.vn.ebookstore.repository.OrderDetailRepository;
import com.vn.ebookstore.service.PaymentDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class PaymentDetailServiceImpl implements PaymentDetailService {

    @Autowired
    private PaymentDetailRepository paymentDetailRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Override
    public PaymentDetail createPayment(Integer orderId, String paymentMethod, BigDecimal amount) {
        return null;
    }

    @Override
    public PaymentDetail getPaymentById(Integer id) {
        return paymentDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Override
    public List<PaymentDetail> getPaymentsByOrderId(Integer orderId) {
        return paymentDetailRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public PaymentDetail updatePaymentStatus(Integer paymentId, String status) {
        PaymentDetail payment = getPaymentById(paymentId);
        payment.setStatus(status);
        payment.setUpdatedAt(new Date());

        // Nếu thanh toán thành công, cập nhật trạng thái đơn hàng
        if ("SUCCESS".equals(status)) {
            OrderDetail order = payment.getOrder();
            order.setStatus("CONFIRMED");
            orderDetailRepository.save(order);
        }

        return paymentDetailRepository.save(payment);
    }

    @Override
    public PaymentDetail save(PaymentDetail payment) {
        return paymentDetailRepository.save(payment);
    }
}