package com.vn.ebookstore.service.impl;

import com.vn.ebookstore.model.Category;
import com.vn.ebookstore.model.SubCategory;
import com.vn.ebookstore.repository.CategoryRepository;
import com.vn.ebookstore.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(int id, Category category) {
        Category existingCategory = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        return categoryRepository.save(existingCategory);
    }

    @Override
    public void deleteCategory(int id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public Category getCategoryById(int id) {
        return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAllOrderById();
    }
    @Override
    public boolean hasBooksInCategory(Integer categoryId) {
        Category category = getCategoryById(categoryId);
        for (SubCategory sub : category.getSubCategories()) {
            if (sub.getBooks() != null && !sub.getBooks().isEmpty()) {
                return true;
            }
        }
        return false;
    }

}