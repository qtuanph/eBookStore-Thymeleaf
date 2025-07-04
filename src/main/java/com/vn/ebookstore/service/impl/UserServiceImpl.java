package com.vn.ebookstore.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vn.ebookstore.model.Address;
import com.vn.ebookstore.model.Role;
import com.vn.ebookstore.model.User;
import com.vn.ebookstore.repository.AddressRepository;
import com.vn.ebookstore.repository.RoleRepository;
import com.vn.ebookstore.repository.UserRepository;
import com.vn.ebookstore.security.UserDetailsImpl;
import com.vn.ebookstore.service.UserService;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;

    public UserServiceImpl(UserRepository userRepository, 
                         RoleRepository roleRepository,
                         PasswordEncoder passwordEncoder,
                         AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.addressRepository = addressRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Đang tìm user với email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Không tìm thấy user với email: {}", email);
                    return new UsernameNotFoundException("Không tìm thấy user với email: " + email);
                });

        logger.info("Tìm thấy user: {}", user.getEmail());
        logger.info("Mật khẩu trong database: {}", user.getPassword());
        
        // Load roles một cách eager
        Collection<Role> roles = user.getRoles();
        logger.info("Roles của user: {}", roles.stream().map(Role::getName).collect(Collectors.joining(", ")));

        return new UserDetailsImpl(user, mapRolesToAuthorities(roles));
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
                .collect(Collectors.toList());
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateUser(int id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        
        // Validate email if changed
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            checkEmailExists(user.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        
        // Validate username if changed
        if (!existingUser.getUsername().equals(user.getUsername()) && 
            checkUsernameExists(user.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        // Update basic info
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setBirthOfDate(user.getBirthOfDate());
        existingUser.setPhoneNumber(user.getPhoneNumber());

          //  Cập nhật avatar nếu có
        if (user.getAvatar() != null && !user.getAvatar().trim().isEmpty()) {
            existingUser.setAvatar(user.getAvatar());
        }
        
        existingUser.setRoles(user.getRoles());
        
        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
        for (Address address : user.getAddresses()) {
            address.setUser(existingUser); // cập nhật quan hệ 2 chiều
        }
    
        existingUser.getAddresses().clear(); // xóa hết địa chỉ cũ (nếu muốn giữ thì cần merge)
        existingUser.getAddresses().addAll(user.getAddresses());
    }

        // Handle password separately - don't update if empty
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        try {
            return userRepository.save(existingUser);
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật user: {}", e.getMessage());
            throw new RuntimeException("Không thể cập nhật thông tin người dùng", e);
        }
    }

    @Override
    public void deleteUser(int id) {
        try {
             User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Kiểm tra và clear thủ công nếu cần
        if (user.getAddresses() != null) {
            user.getAddresses().clear();
        }
        if (user.getWishlist() != null) {
            user.getWishlist().clear();
        }
        if (user.getCarts() != null) {
            user.getCarts().clear();
        }
        if (user.getOrders() != null) {
            user.getOrders().clear();
        }
        if (user.getReviews() != null) {
            user.getReviews().clear();
        }

        userRepository.delete(user); // xóa hoàn toàn khỏi DB
        } catch (Exception e) {
            logger.error("Lỗi khi xóa user: {}",id, e.getMessage(),e);
            throw new RuntimeException("Không thể xóa người dùng", e);
        }
    }

    @Override
    public User getUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User registerNewUser(User user) {
        // Validate required fields
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email không được để trống");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Mật khẩu không được để trống");
        }

        // Check email & username
        if (checkEmailExists(user.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (user.getUsername() != null && checkUsernameExists(user.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        try {
            // Set default values
            user.setCreatedAt(new Date());
            user.setUsername(user.getUsername() != null ? user.getUsername() : user.getEmail());
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Set default ROLE_CUSTOMER role 
            Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ROLE_CUSTOMER");
                    newRole.setDescription("Regular customer role");
                    newRole.setCreatedAt(new Date());
                    return roleRepository.save(newRole);
                });

            user.setRoles(Arrays.asList(customerRole));
            
            // Save user first to get ID
            user = userRepository.save(user);

            // Save addresses if any
            if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
                for (Address address : user.getAddresses()) {
                    address.setUser(user);
                    address.setCreatedAt(new Date());
                    addressRepository.save(address);
                }
            }

            logger.info("Created new user with email: {}", user.getEmail());
            return user;

        } catch (Exception e) {
            logger.error("Error creating new user: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo người dùng mới", e);
        }
    }

    @Override
    public boolean checkEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        try {
            logger.info("Kiểm tra mật khẩu - Raw: {}, Encoded: {}", rawPassword, encodedPassword);
            
            // Thử so sánh trực tiếp nếu mật khẩu chưa mã hóa
            if (rawPassword.equals(encodedPassword)) {
                logger.info("Mật khẩu khớp (chưa mã hóa)");
                return true;
            }
            
            // Nếu không khớp, thử so sánh với mật khẩu đã mã hóa
            boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
            logger.info("Kết quả so sánh mật khẩu đã mã hóa: {}", matches);
            return matches;
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra mật khẩu: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với email: " + email));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public long getTotalUsers() {
        return userRepository.countByDeletedAtIsNull();
    }

    @Override
    public Page<User> getAllUsersPage(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.findByFirstNameContainingOrLastNameContainingOrEmailContainingOrUsernameContaining(
            keyword, keyword, keyword, keyword, pageable);
    }

    @Override
    public Page<User> findByRole(String role, Pageable pageable) {
        return userRepository.findByRolesName(role, pageable);
    }

    @Override
    public User updateUserRoles(int userId, List<Integer> roleIds) {
        User user = getUserById(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            throw new RuntimeException("Phải có ít nhất một vai trò");
        }

        try {
            Set<Role> newRoles = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy role với ID: " + roleId)))
                .collect(Collectors.toSet());

            user.setRoles(newRoles.stream().collect(Collectors.toList()));
            return userRepository.save(user);
        } catch (Exception e) {
            logger.error("Error updating user roles: {}", e.getMessage());
            throw new RuntimeException("Không thể cập nhật vai trò người dùng", e);
        }
    }

    @Override
    public User addRolesToUser(int userId, List<Integer> roleIds) {
        User user = getUserById(userId);
        Set<Role> currentRoles = new HashSet<>(user.getRoles());

        try {
            Set<Role> newRoles = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy role với ID: " + roleId)))
                .collect(Collectors.toSet());

            currentRoles.addAll(newRoles);
            user.setRoles(currentRoles.stream().collect(Collectors.toList()));
            return userRepository.save(user);

        } catch (Exception e) {
            logger.error("Error adding roles to user: {}", e.getMessage());
            throw new RuntimeException("Không thể thêm vai trò cho người dùng", e);
        }
    }

    @Override
    public User removeRolesFromUser(int userId, List<Integer> roleIds) {
        User user = getUserById(userId);
        Set<Role> currentRoles = new HashSet<>(user.getRoles());

        if (currentRoles.size() <= roleIds.size()) {
            throw new RuntimeException("Không thể xóa tất cả vai trò của người dùng");
        }

        try {
            Set<Role> rolesToRemove = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy role với ID: " + roleId)))
                .collect(Collectors.toSet());

            currentRoles.removeAll(rolesToRemove);
            user.setRoles(currentRoles.stream().collect(Collectors.toList()));
            return userRepository.save(user);

        } catch (Exception e) {
            logger.error("Error removing roles from user: {}", e.getMessage());
            throw new RuntimeException("Không thể xóa vai trò của người dùng", e);
        }
    }
}