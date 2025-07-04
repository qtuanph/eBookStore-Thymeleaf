package com.vn.ebookstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.vn.ebookstore.repository")
@SpringBootApplication
public class EBookStoreThymeleafApplication {

    public static void main(String[] args) {
        SpringApplication.run(EBookStoreThymeleafApplication.class, args);
    }

}
