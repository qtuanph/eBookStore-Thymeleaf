package com.vn.ebookstore.controller.admin;

import com.vn.ebookstore.model.*;
import com.vn.ebookstore.service.*;
import com.vn.ebookstore.repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            // Tổng số đơn hàng
            long totalOrders = orderDetailService.getTotalOrders();

            // Tổng doanh thu
            BigDecimal totalRevenue = orderDetailService.getTotalRevenue();

            // Tổng số người dùng
            long totalUsers = userService.getTotalUsers();

            // Tổng số sản phẩm
            long totalProducts = bookService.getTotalBooks();

            // Lấy tất cả đơn hàng
            PageRequest allOrdersRequest = PageRequest.of(0, Integer.MAX_VALUE);
            List<OrderDetail> allOrders = orderDetailRepository.findTopNByOrderByCreatedAtDesc(allOrdersRequest);
            model.addAttribute("recentOrders", allOrders);

            // Lấy dữ liệu doanh thu 7 ngày
            PageRequest last7DaysRequest = PageRequest.of(0, 7);
            List<OrderDetail> ordersLast7Days = orderDetailRepository.findTopNByOrderByCreatedAtDesc(last7DaysRequest);
            List<BigDecimal> salesData = new ArrayList<>();
            for (OrderDetail order : ordersLast7Days) {
                salesData.add(order.getTotal());
            }

            // Thống kê trạng thái đơn hàng
            List<Long> orderStatusData = new ArrayList<>();
            orderStatusData.add(orderDetailService.countOrdersByStatus("PENDING"));
            orderStatusData.add(orderDetailService.countOrdersByStatus("CONFIRMED"));
            orderStatusData.add(orderDetailService.countOrdersByStatus("SHIPPING"));
            orderStatusData.add(orderDetailService.countOrdersByStatus("DELIVERED"));
            orderStatusData.add(orderDetailService.countOrdersByStatus("CANCELLED"));

            // Thêm dữ liệu vào model
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("salesData", salesData);
            model.addAttribute("orderStatusData", orderStatusData);

            return "page/admin/dashboard";

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải dữ liệu: " + e.getMessage());
            return "page/admin/dashboard";
        }
    }
}