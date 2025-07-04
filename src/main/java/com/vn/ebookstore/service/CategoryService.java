package com.vn.ebookstore.service;

import com.vn.ebookstore.model.Category;
import java.util.List;

public interface CategoryService {
    Category createCategory(Category category);
    Category updateCategory(int id, Category category);
    void deleteCategory(int id);
    Category getCategoryById(int id);
    List<Category> getAllCategories();
    boolean hasBooksInCategory(Integer categoryId);

}