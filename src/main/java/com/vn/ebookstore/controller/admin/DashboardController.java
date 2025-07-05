package com.vn.ebookstore.controller.admin;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.vn.ebookstore.model.OrderDetail;
import com.vn.ebookstore.service.BookService;
import com.vn.ebookstore.service.OrderDetailService;
import com.vn.ebookstore.service.UserService;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            // Tổng số liệu
            long totalOrders = orderDetailService.getTotalOrders();
            BigDecimal totalRevenue = orderDetailService.getTotalRevenue();
            long totalUsers = userService.getTotalUsers(); // bạn nên đảm bảo method này tồn tại
            long totalProducts = bookService.getTotalBooks(); // method này cũng vậy

            // Đơn hàng gần đây (hiện 5 cái gần nhất)
            List<OrderDetail> recentOrders = orderDetailService.getRecentOrders(PageRequest.of(0, 5));

            // Sales data: lấy total của 7 đơn gần nhất (giả lập biểu đồ)
            List<OrderDetail> ordersLast7Days = orderDetailService.getRecentOrders(PageRequest.of(0, 7));
            Map<String, BigDecimal> salesData = new LinkedHashMap<>();
            for (int i = ordersLast7Days.size() - 1; i >= 0; i--) {
                OrderDetail order = ordersLast7Days.get(i);
                String label = "Đơn #" + order.getId();
                salesData.put(label, order.getTotal());
            }

            // Thống kê theo trạng thái đơn hàng
            Map<String, Long> orderStatusData = new LinkedHashMap<>();
            orderStatusData.put("PENDING", orderDetailService.countOrdersByStatus("PENDING"));
            orderStatusData.put("CONFIRMED", orderDetailService.countOrdersByStatus("CONFIRMED"));
            orderStatusData.put("SHIPPING", orderDetailService.countOrdersByStatus("SHIPPING"));
            orderStatusData.put("DELIVERED", orderDetailService.countOrdersByStatus("DELIVERED"));
            orderStatusData.put("CANCELLED", orderDetailService.countOrdersByStatus("CANCELLED"));

            // Đưa vào model
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("recentOrders", recentOrders);
            model.addAttribute("salesData", salesData);
            model.addAttribute("orderStatusData", orderStatusData);

            return "page/admin/dashboard";

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải dữ liệu: " + e.getMessage());
            return "page/admin/dashboard";
        }
    }
}
