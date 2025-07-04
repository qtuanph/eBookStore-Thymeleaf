package com.vn.ebookstore.config;

import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối tới thư mục gốc của project
        String baseUploadPath = Paths.get(System.getProperty("user.dir"), "uploads").toString();

        // Cấu hình ánh xạ ảnh avatar
        registry.addResourceHandler("/image/avatar/**")
                .addResourceLocations("file:///" + baseUploadPath + "/avatar/");

        // Cấu hình ánh xạ ảnh category
        registry.addResourceHandler("/image/category/**")
                .addResourceLocations("file:///" + baseUploadPath + "/category/");

        // Cấu hình ánh xạ ảnh sách (book cover)
        registry.addResourceHandler("/image/book/**")
                .addResourceLocations("file:///" + baseUploadPath + "/book/");
    }
}
