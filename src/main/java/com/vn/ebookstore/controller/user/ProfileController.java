package com.vn.ebookstore.controller.user;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.ebookstore.model.Address;
import com.vn.ebookstore.model.Cart;
import com.vn.ebookstore.model.Category;
import com.vn.ebookstore.model.User;
import com.vn.ebookstore.model.Wishlist;
import com.vn.ebookstore.security.UserDetailsImpl;
import com.vn.ebookstore.service.CartService;
import com.vn.ebookstore.service.CategoryService;
import com.vn.ebookstore.service.UserService;
import com.vn.ebookstore.service.WishlistService;

@Controller
@RequestMapping("/user")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private UserService userService;
    @Autowired
    private CartService cartService;
    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            model.addAttribute("cart", cart);
            model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
            model.addAttribute("user", user);
        }
        return "page/user/profile";
    }

    @GetMapping("/settings")
    public String showSettings(Model model, Principal principal) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            Cart cart = cartService.getCurrentCartByUser(user);
            List<Wishlist> wishlists = wishlistService.getWishlistsByUser(user);
            model.addAttribute("cart", cart);
            model.addAttribute("wishlists", wishlists != null ? wishlists : new ArrayList<>());
        }
        return "page/user/settings";
    }

    @PostMapping("/settings/update")
    public String updatePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng");
            return "redirect:/user/settings";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp");
            return "redirect:/user/settings";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);

        redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công");
        return "redirect:/user/settings";
    }

    @PostMapping("/profile/update-username")
    public String updateUsername(@RequestParam("newUsername") String newUsername,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByEmail(principal.getName());

            if (!passwordEncoder.matches(confirmPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu không đúng");
                return "redirect:/user/profile";
            }

            if (userService.existsByUsername(newUsername)) {
                redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại");
                return "redirect:/user/profile";
            }

            user.setUsername(newUsername);
            userService.save(user);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật tên đăng nhập thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/profile";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("birthOfDate") String birthOfDate,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam("addressLine") String addressLine,
            @RequestParam("ward") String ward,
            @RequestParam("district") String district,
            @RequestParam("city") String city,
            @RequestParam("country") String country,
            @RequestParam("postalCode") String postalCode,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getUserByEmail(principal.getName());

            if (!email.equals(currentUser.getEmail()) && userService.checkEmailExists(email)) {
                redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng");
                return "redirect:/user/profile";
            }

            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            currentUser.setEmail(email);
            currentUser.setPhoneNumber(phoneNumber);

            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                dateFormat.setLenient(false);
                Date date = dateFormat.parse(birthOfDate);
                if (date.after(new Date())) {
                    throw new IllegalArgumentException("Ngày sinh không thể là ngày trong tương lai");
                }
                currentUser.setBirthOfDate(date);
            } catch (ParseException e) {
                redirectAttributes.addFlashAttribute("error", "Định dạng ngày sinh không hợp lệ (dd/MM/yyyy)");
                return "redirect:/user/profile";
            }

            if (avatar != null && !avatar.isEmpty()) {
                String uploadDir = "src/main/resources/static/image/avatar";
                String oldAvatarPath = null;

                if (currentUser.getAvatar() != null) {
                    oldAvatarPath = "src/main/resources/static" + currentUser.getAvatar();
                }

                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String originalFilename = avatar.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String filename = UUID.randomUUID().toString() + extension;

                Path filePath = uploadPath.resolve(filename);
                Files.copy(avatar.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                currentUser.setAvatar("/image/avatar/" + filename);

                if (oldAvatarPath != null) {
                    try {
                        Files.deleteIfExists(Paths.get(oldAvatarPath));
                    } catch (IOException e) {
                        logger.warn("Không thể xóa avatar cũ: " + e.getMessage());
                    }
                }
            }

            if (currentUser.getAddresses() == null || currentUser.getAddresses().isEmpty()) {
                Address address = new Address();
                address.setUser(currentUser);
                address.setCreatedAt(new Date());
                currentUser.setAddresses(Arrays.asList(address));
            }
            Address address = currentUser.getAddresses().get(0);
            address.setAddressLine(addressLine);
            address.setWard(ward);
            address.setDistrict(district);
            address.setCity(city);
            address.setCountry(country);
            address.setPostalCode(postalCode);
            address.setCreatedAt(new Date());

            userService.save(currentUser);

            UserDetailsImpl userDetails = new UserDetailsImpl(currentUser, new ArrayList<>());
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            redirectAttributes.addFlashAttribute("success", "Thông tin cá nhân đã được cập nhật");

        } catch (IOException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/user/profile";
        }
        return "redirect:/user/profile";
    }
}
