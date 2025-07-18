package com.vn.ebookstore.controller.user;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.ebookstore.model.Address;
import com.vn.ebookstore.model.User;
import com.vn.ebookstore.service.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/avatar";
    private final String ACCESS_PATH = "/image/avatar/";

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
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
                return "redirect:/register";
            }

            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password); // Gửi raw password
            user.setPhoneNumber(phoneNumber);

            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date birthDate = dateFormat.parse(birthOfDate);
                user.setBirthOfDate(birthDate);
            } catch (java.text.ParseException | NullPointerException e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Định dạng ngày sinh không hợp lệ!");
                return "redirect:/register";
            }

            if (!avatar.isEmpty()) {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String originalFilename = avatar.getOriginalFilename();
                if (originalFilename == null || !originalFilename.contains(".")) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Tên file ảnh không hợp lệ!");
                    return "redirect:/register";
                }
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String filename = UUID.randomUUID().toString() + extension;

                Path filePath = uploadPath.resolve(filename);
                Files.copy(avatar.getInputStream(), filePath);

                user.setAvatar(ACCESS_PATH + filename);
            }

            Address address = new Address();
            address.setAddressLine(addressLine);
            address.setWard(ward);
            address.setDistrict(district);
            address.setCity(city);
            address.setCountry(country);
            address.setPostalCode(postalCode);
            address.setUser(user);

            List<Address> addresses = new ArrayList<>();
            addresses.add(address);
            user.setAddresses(addresses);

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
