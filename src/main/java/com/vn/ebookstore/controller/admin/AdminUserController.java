package com.vn.ebookstore.controller.admin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.vn.ebookstore.model.Address;
import com.vn.ebookstore.model.User;
import com.vn.ebookstore.service.RoleService;
import com.vn.ebookstore.service.UserService;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @GetMapping("")
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String success,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage;

        if (search != null && !search.isEmpty()) {
            userPage = userService.searchUsers(search, pageRequest);
        } else if (role != null && !role.isEmpty()) {
            userPage = userService.findByRole(role, pageRequest);
        } else {
            userPage = userService.getAllUsersPage(pageRequest);
        }

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("roles", roleService.getAllRoles());
        
        if ("true".equals(success)) {
            model.addAttribute("successMessage", "Cập nhật người dùng thành công!");
        }

        return "page/admin/users/list-users";
    }

    @GetMapping("/create")
    public String createUserForm(Model model) {
        User user = new User();
        Address address = new Address();
        List<Address> addresses = new ArrayList<>();
        addresses.add(address);
        user.setAddresses(addresses);

        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleService.getAllRoles());
        return "page/admin/users/create-users";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute("user") User user,
                            @RequestParam("avatarFile") MultipartFile avatarFile) {
    // 1. Lưu tên file vào thuộc tính user trước
    if (!avatarFile.isEmpty()) {
        String fileName = avatarFile.getOriginalFilename();
        user.setAvatar(fileName);  //  Đảm bảo gán vào entity trước khi lưu DB
    }

    // 2. Gắn user cho từng địa chỉ (nếu có)
    if (user.getAddresses() != null) {
        for (Address address : user.getAddresses()) {
            address.setUser(user);
        }
    }

    // 3. Lưu vào DB trước để lấy ID (nếu cần)
    User savedUser = userService.registerNewUser(user);

    // 4. Lưu file vào resources/static/image/avatar
    if (!avatarFile.isEmpty()) {
        try {
            Path uploadPath = Paths.get("E:/suasang/eBookStore-Thymeleaf/uploads/avatar");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(avatarFile.getOriginalFilename());
            avatarFile.transferTo(filePath.toFile());

            System.out.println("Avatar saved to: " + filePath.toAbsolutePath());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    return "redirect:/admin/users";
        
    }


    @GetMapping("/{id}")
    @ResponseBody
    public User getUser(@PathVariable int id) {
        return userService.getUserById(id);
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable int id, Model model) {
        User user = userService.getUserById(id);
        if (user.getAddresses().isEmpty()) {
            user.getAddresses().add(new Address());
        }
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleService.getAllRoles());
        return "page/admin/users/edit-users";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable int id, @ModelAttribute("user") User updatedUser,
                            @RequestParam("avatarFile") MultipartFile avatarFile) throws IOException {
        updatedUser.setId(id); // đảm bảo ID được giữ lại

        if (updatedUser.getAddresses() != null) {
            for (Address address : updatedUser.getAddresses()) {
                address.setUser(updatedUser);
            }
        }

          User existingUser = userService.getUserById(id);

        if (avatarFile != null && !avatarFile.isEmpty()) {
        String fileName = avatarFile.getOriginalFilename();
        updatedUser.setAvatar(fileName);

        Path uploadPath = Paths.get("E:/suasang/eBookStore-Thymeleaf/uploads/avatar");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = avatarFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Avatar saved to: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    } else {
        updatedUser.setAvatar(existingUser.getAvatar());
    }

        userService.updateUser(id, updatedUser);

        return "redirect:/admin/users?success=true";
    }


    @GetMapping("delete/{id}")
    public String deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return "redirect:/admin/users?success=true";
    }
}
