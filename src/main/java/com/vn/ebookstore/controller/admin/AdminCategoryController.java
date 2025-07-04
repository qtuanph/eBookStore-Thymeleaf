package com.vn.ebookstore.controller.admin;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.ebookstore.model.Category;
import com.vn.ebookstore.model.SubCategory;
import com.vn.ebookstore.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {
  private final CategoryService categoryService;

    // Constructor injection (nên dùng)
    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ModelAndView listCategories() {
        ModelAndView mav = new ModelAndView("page/admin/categories/categories");
        mav.addObject("categories", categoryService.getAllCategories());
        return mav;
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        Category category = new Category();
        category.getSubCategories().add(new SubCategory()); // tạo sẵn 1 danh mục con
        model.addAttribute("category", category);
        return "page/admin/categories/category-form";
    }

    @PostMapping("/save")
   public String saveCategory(@ModelAttribute("category") Category category,
                               @RequestParam("backgroundImage") MultipartFile coverFile) throws IOException {
        
       
    // Nếu là sửa (tức là category đã có id)
    if (category.getId() != null) {
        Category existing = categoryService.getCategoryById(category.getId());
        if (existing != null && (coverFile == null || coverFile.isEmpty())) {
            // Nếu không chọn ảnh mới -> giữ lại ảnh cũ
            category.setImage(existing.getImage());
        }
    }

    // Nếu người dùng có upload ảnh mới thì thay thế ảnh
    if (coverFile != null && !coverFile.isEmpty()) {
        String fileName = coverFile.getOriginalFilename();
        String uploadPath = "E:/suasang/eBookStore-Thymeleaf/uploads/category";

        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File saveFile = new File(uploadPath, fileName);
        coverFile.transferTo(saveFile);

        category.setImage(fileName); // Gán ảnh mới
    }

    // Gắn lại quan hệ 2 chiều với SubCategory
    List<SubCategory> subs = category.getSubCategories();
    if (subs != null) {
        for (SubCategory sub : subs) {
            sub.setCategory(category);
        }
    }

    categoryService.createCategory(category);
    return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id,Model model) {
            Category category;
        try {
            category = categoryService.getCategoryById(id);
        } catch (Exception e) {
            return "error/404";  // Hoặc trang lỗi khác bạn có
        }
        if (category.getSubCategories().isEmpty()) {
            category.getSubCategories().add(new SubCategory());
        }
        model.addAttribute("category", category);
        return "page/admin/categories/category-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Integer id,RedirectAttributes redirectAttrs) {
       Category category = categoryService.getCategoryById(id);
    if (category == null) {
        redirectAttrs.addFlashAttribute("error", "Không tìm thấy danh mục.");
        return "redirect:/admin/categories";
    }

     // Kiểm tra nếu còn sách thì không cho xóa
    if (categoryService.hasBooksInCategory(id)) {
        redirectAttrs.addFlashAttribute("error", "Không thể xóa vì danh mục vẫn còn sách.");
        return "redirect:/admin/categories";
    }

    try {
        categoryService.deleteCategory(id);
        redirectAttrs.addFlashAttribute("success", "Xóa danh mục thành công.");
    } catch (Exception e) {
        redirectAttrs.addFlashAttribute("error", "Không thể xóa danh mục. Có thể đang được sử dụng.");
    }

    return "redirect:/admin/categories";
    }
}
