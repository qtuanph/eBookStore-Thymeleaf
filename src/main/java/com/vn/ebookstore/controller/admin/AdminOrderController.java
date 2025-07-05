package com.vn.ebookstore.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vn.ebookstore.model.OrderDetail;
import com.vn.ebookstore.service.OrderDetailService;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderDetailService orderDetailService;

    // GET: Danh sách tất cả đơn hàng
    @GetMapping
    public String listOrders(Model model) {
        List<OrderDetail> orders = orderDetailService.getAllOrders();
        model.addAttribute("orders", orders);
        return "page/admin/orders/orders"; // Thymeleaf file
    }

    // GET: Chi tiết 1 đơn hàng theo ID
    @GetMapping("/{id}")
    public String viewOrder(@PathVariable("id") int id, Model model) {
        try {
            OrderDetail order = orderDetailService.getOrderById(id);
            model.addAttribute("order", order);
            return "page/admin/orders/order_detail";
        } catch (Exception e) {
            model.addAttribute("error", "Không tìm thấy đơn hàng #" + id);
            return "redirect:/admin/orders";
        }
    }

    // POST: Hủy đơn hàng (PENDING hoặc CONFIRMED)
    @PostMapping("/{id}/delete")
    public String cancelOrder(@PathVariable("id") int id) {
        try {
            orderDetailService.cancelOrder(id);
            return "redirect:/admin/orders?success=cancel";
        } catch (Exception e) {
            return "redirect:/admin/orders?error=cancel";
        }
    }

    // POST: Cập nhật trạng thái đơn hàng (tổng quát)
    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(@PathVariable("id") int id,
                                    @RequestParam("status") String status) {
        try {
            orderDetailService.updateOrderStatus(id, status);
            return "redirect:/admin/orders/" + id + "?success=updated";
        } catch (Exception e) {
            return "redirect:/admin/orders/" + id + "?error=update";
        }
    }

    // POST: Chuyển trạng thái từ CONFIRMED → SHIPPING (Admin)
    @PostMapping("/{id}/ship")
    public String shipOrder(@PathVariable("id") Integer id) {
        try {
            OrderDetail order = orderDetailService.getOrderById(id);
            if ("CONFIRMED".equals(order.getStatus())) {
                orderDetailService.updateOrderStatus(id, "SHIPPING");
                return "redirect:/admin/orders/" + id + "?shipped";
            } else {
                return "redirect:/admin/orders/" + id + "?error=invalid_status";
            }
        } catch (Exception e) {
            return "redirect:/admin/orders/" + id + "?error=ship";
        }
    }
}
