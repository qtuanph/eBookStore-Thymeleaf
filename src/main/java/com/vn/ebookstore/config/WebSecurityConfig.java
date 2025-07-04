package com.vn.ebookstore.config;

import com.vn.ebookstore.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    
    private final UserService userService;

    public WebSecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.disable())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Static resources
                .requestMatchers(
                    "/css/**", 
                    "/js/**",
                    "/image/**",
                    "/vendor/**",
                    "/fonts/**",
                    "/webjars/**",
                    "/error"
                ).permitAll()
                
                // Public pages & APIs
                .requestMatchers(
                    "/",
                    "/index",
                    "/home",
                    "/about_us",
                    "/products", 
                    "/product/**",
                    "/category/**",
                    "/subcategory/**",
                    "/faq",
                    "/login",
                    "/register",
                    "/api/books/search",
                    "/api/coupons/validate"
                ).permitAll()
                
                // Admin pages
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Authenticated pages  
                .requestMatchers(
                    "/user/**",
                    "/cart/**", 
                    "/checkout/**",
                    "/wishlist/**",
                    "/order/**", 
                    "/review/**"
                ).authenticated()
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") 
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                // Thay đổi phần này để xử lý redirect dựa trên role
                .successHandler((_, response, authentication) -> {
                    if (authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                        response.sendRedirect("/admin/dashboard");
                    } else {
                        response.sendRedirect("/user/home");
                    }
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );
            
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder
            .userDetailsService((UserDetailsService) userService)
            .passwordEncoder(passwordEncoder());
        return authManagerBuilder.build();
    }
}
