package com.vn.ebookstore.controller.admin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);
    private static final Path AVATAR_UPLOAD_PATH = Paths.get(System.getProperty("user.dir"), "uploads", "avatar");

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

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                ensureDirectoryExists(AVATAR_UPLOAD_PATH);
                String fileName = avatarFile.getOriginalFilename();
                Path filePath = AVATAR_UPLOAD_PATH.resolve(fileName);
                avatarFile.transferTo(filePath.toFile());
                user.setAvatar(fileName);
            } catch (IOException e) {
                logger.error("Failed to save avatar file", e);
            }
        }

        if (user.getAddresses() != null) {
            for (Address address : user.getAddresses()) {
                address.setUser(user);
            }
        }

        userService.save(user);
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
    public String updateUser(@PathVariable int id,
                             @ModelAttribute("user") User updatedUser,
                             @RequestParam("avatarFile") MultipartFile avatarFile) throws IOException {

        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return "redirect:/admin/users?error=notfound";
        }

        updatedUser.setId(id);

        if (updatedUser.getAddresses() != null) {
            for (Address address : updatedUser.getAddresses()) {
                address.setUser(updatedUser);
            }
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            ensureDirectoryExists(AVATAR_UPLOAD_PATH);
            String fileName = avatarFile.getOriginalFilename();

            // Xoá avatar cũ nếu tồn tại
            if (existingUser.getAvatar() != null) {
                Path oldAvatarPath = AVATAR_UPLOAD_PATH.resolve(existingUser.getAvatar());
                if (Files.exists(oldAvatarPath)) {
                    Files.delete(oldAvatarPath);
                }
            }

            // Lưu avatar mới
            Path newAvatarPath = AVATAR_UPLOAD_PATH.resolve(fileName);
            try (InputStream inputStream = avatarFile.getInputStream()) {
                Files.copy(inputStream, newAvatarPath, StandardCopyOption.REPLACE_EXISTING);
            }
            updatedUser.setAvatar(fileName);
        } else {
            updatedUser.setAvatar(existingUser.getAvatar());
        }

        userService.updateUser(id, updatedUser);
        return "redirect:/admin/users?success=true";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return "redirect:/admin/users?success=true";
    }

    private void ensureDirectoryExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }
}
