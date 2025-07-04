package com.vn.ebookstore.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
         // Thư mục ảnh thực trên máy
        Path uploadDir = Paths.get("E:/suasang/eBookStore-Thymeleaf/uploads/avatar");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // ánh xạ URL /image/avatar/** -> thư mục ảnh ngoài
        registry.addResourceHandler("/image/avatar/**")
                .addResourceLocations("file:///" + uploadPath + "/");

         // ánh xạ ảnh danh mục
        Path categoryDir = Paths.get("E:/suasang/eBookStore-Thymeleaf/uploads/category");
        String categoryPath = categoryDir.toFile().getAbsolutePath();
        registry.addResourceHandler("/image/category/**")
                .addResourceLocations("file:///" + categoryPath + "/");

        // ánh xạ ảnh sách (book cover)
        Path bookDir = Paths.get("E:/suasang/eBookStore-Thymeleaf/uploads/book");
        String bookPath = bookDir.toFile().getAbsolutePath();
        registry.addResourceHandler("/image/book/**")
                .addResourceLocations("file:///" + bookPath + "/");

    }
}
