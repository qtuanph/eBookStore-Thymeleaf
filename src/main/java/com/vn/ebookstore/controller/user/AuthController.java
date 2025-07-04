package com.vn.ebookstore.controller.user;

import com.vn.ebookstore.model.User;
import com.vn.ebookstore.model.Address;
import com.vn.ebookstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @GetMapping("/login")
    public String login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            // Kiểm tra role và redirect tương ứng
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/user/home";
            }
        }
        return "page/auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "page/auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam("firstName") String firstName,
                             @RequestParam("lastName") String lastName,
                             @RequestParam("username") String username,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             @RequestParam("confirmPassword") String confirmPassword,
                             @RequestParam("birthOfDate") String birthOfDate,
                             @RequestParam("phoneNumber") String phoneNumber,
                             @RequestParam("avatar") MultipartFile avatar,
                             @RequestParam("addressLine") String addressLine,
                             @RequestParam("ward") String ward,
                             @RequestParam("district") String district,
                             @RequestParam("city") String city,
                             @RequestParam("country") String country,
                             @RequestParam("postalCode") String postalCode,
                             RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra mật khẩu xác nhận
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
                return "redirect:/register";
            }

            // Tạo đối tượng User
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setPhoneNumber(phoneNumber);

            // Parse ngày sinh
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date birthDate = dateFormat.parse(birthOfDate);
                user.setBirthOfDate(birthDate);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Định dạng ngày sinh không hợp lệ!");
                return "redirect:/register";
            }

            // Xử lý upload ảnh
            if (!avatar.isEmpty()) {
                String uploadDir = "src/main/resources/static/image/avatar";
                
                // Xử lý xóa avatar cũ nếu có
                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    try {
                        String oldAvatarPath = "src/main/resources/static" + user.getAvatar();
                        Files.deleteIfExists(Paths.get(oldAvatarPath));
                    } catch (IOException e) {
                        // Log warning nhưng vẫn tiếp tục xử lý
                        System.out.println("Warning: Could not delete old avatar: " + e.getMessage());
                    }
                }

                // Tạo thư mục nếu chưa tồn tại
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Tạo tên file ngẫu nhiên để tránh trùng lặp
                String originalFilename = avatar.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String filename = UUID.randomUUID().toString() + extension;

                // Lưu file mới
                Path filePath = uploadPath.resolve(filename);
                Files.copy(avatar.getInputStream(), filePath);

                // Cập nhật đường dẫn avatar mới
                user.setAvatar("/image/avatar/" + filename);
            }

            // Tạo đối tượng Address và thiết lập thông tin
            Address address = new Address();
            address.setAddressLine(addressLine);
            address.setWard(ward);
            address.setDistrict(district);
            address.setCity(city);
            address.setCountry(country);
            address.setPostalCode(postalCode);
            address.setUser(user);

            // Thiết lập danh sách địa chỉ cho user
            List<Address> addresses = new ArrayList<>();
            addresses.add(address);
            user.setAddresses(addresses);

            // Đăng ký user mới
            userService.registerNewUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi upload ảnh: " + e.getMessage());
            return "redirect:/register";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }
}