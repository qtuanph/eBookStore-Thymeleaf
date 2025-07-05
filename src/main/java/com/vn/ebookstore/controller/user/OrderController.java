package com.vn.ebookstore.controller.user;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.ebookstore.model.Address;
import com.vn.ebookstore.model.Cart;
import com.vn.ebookstore.model.Category;
import com.vn.ebookstore.model.Coupon;
import com.vn.ebookstore.model.OrderDetail;
import com.vn.ebookstore.model.PaymentDetail;
import com.vn.ebookstore.model.User;
import com.vn.ebookstore.model.Wishlist;
import com.vn.ebookstore.service.AddressService;
import com.vn.ebookstore.service.CartService;
import com.vn.ebookstore.service.CategoryService;
import com.vn.ebookstore.service.CouponService;
import com.vn.ebookstore.service.OrderDetailService;
import com.vn.ebookstore.service.PaymentDetailService;
import com.vn.ebookstore.service.UserService;
import com.vn.ebookstore.service.WishlistService;

@Controller
@RequestMapping("/user")
public class OrderController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private UserService userService;
    @Autowired
    private CartService cartService;
    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private PaymentDetailService paymentDetailService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private CouponService couponService;
    
    @GetMapping("/purchase")
    public String showPurchaseForm(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        User user = userService.getUserByEmail(principal.getName());
        Cart cart = cartService.getCurrentCartByUser(user);
        List<Address> addresses = addressService.getAddressesByUserId(user.getId());
        List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);

        model.addAttribute("cart", cart);
        model.addAttribute("addresses", addresses);
        model.addAttribute("wishlists", wishlists);

        return "page/user/purchase";
    }

    @PostMapping("/purchase/confirm")
    public String confirmPurchase(
            @RequestParam("addressId") Integer addressId,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "couponCode", required = false) String couponCode,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);

            if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống!");
                return "redirect:/user/cart";
            }

            Address shippingAddress = addressService.getAddressById(addressId);
            if (shippingAddress == null || !Objects.equals(shippingAddress.getUser().getId(), user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Địa chỉ không hợp lệ!");
                return "redirect:/user/purchase";
            }

            Coupon coupon = null;
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                Optional<Coupon> couponOpt = couponService.findByCode(couponCode);
                if (couponOpt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Mã giảm giá không hợp lệ!");
                    return "redirect:/user/purchase";
                }
                coupon = couponOpt.get();
                BigDecimal subtotal = cart.getCartItems().stream()
                        .filter(item -> item.getUpdatedAt() == null)
                        .map(item -> new BigDecimal(item.getPrice()).multiply(new BigDecimal(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (!couponService.isValidForUse(coupon, subtotal)) {
                    redirectAttributes.addFlashAttribute("error", "Mã giảm giá không thể sử dụng!");
                    return "redirect:/user/purchase";
                }
            }

            OrderDetail order = orderDetailService.createOrder(user.getId(), addressId, paymentMethod, note, coupon);
            if (order != null) {
                if (coupon != null) {
                    couponService.useCoupon(coupon);
                }
                cartService.deleteCart(cart.getId());
                Cart newCart = new Cart();
                newCart.setUser(user);
                newCart.setCreatedAt(new Date());
                cartService.save(newCart);

                redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công!");
                return "redirect:/user/orders";
            } else {
                throw new RuntimeException("Không thể tạo đơn hàng!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/user/purchase";
        }
    }

    @GetMapping("/orders")
    public String showOrders(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        User user = userService.getUserByEmail(principal.getName());
        List<OrderDetail> orders = orderDetailService.getOrdersByUserId(user.getId());
        Cart cart = cartService.getCurrentCartByUser(user);
        List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);

        model.addAttribute("orders", orders);
        model.addAttribute("cart", cart);
        model.addAttribute("wishlists", wishlists);

        return "page/user/order";
    }

    @PostMapping("/order/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelOrder(@PathVariable Integer orderId, Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            OrderDetail order = orderDetailService.getOrderById(orderId);

            if (order == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Không tìm thấy đơn hàng!"));
            }

            if (!Objects.equals(order.getUser().getId(), user.getId())) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Không có quyền hủy đơn hàng này"));
            }

            if (!("PENDING".equals(order.getStatus()) || "CONFIRMED".equals(order.getStatus()))) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Chỉ có thể hủy đơn hàng ở trạng thái chờ xử lý hoặc đã xác nhận"));
            }

            orderDetailService.cancelOrder(orderId);
            return ResponseEntity.ok()
                    .body(Map.of("message", "Hủy đơn hàng thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/order/{orderId}/pay")
    @ResponseBody
    public ResponseEntity<?> processPayment(@PathVariable Integer orderId, Principal principal) {
        try {
            User user = userService.getUserByEmail(principal.getName());
            OrderDetail order = orderDetailService.getOrderById(orderId);

            if (order == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Không tìm thấy đơn hàng!"));
            }

            if (!Objects.equals(order.getUser().getId(), user.getId())) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Không có quyền thanh toán đơn hàng này"));
            }

            PaymentDetail payment = order.getPayments().get(0);
            paymentDetailService.updatePaymentStatus(payment.getId(), "SUCCESS");

            return ResponseEntity.ok()
                    .body(Map.of("message", "Thanh toán thành công"));
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
