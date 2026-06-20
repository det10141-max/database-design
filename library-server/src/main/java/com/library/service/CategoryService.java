package com.library.service;
import com.library.dto.request.CategorySaveRequest;
import com.library.entity.Category;
import java.util.List;
public interface CategoryService {
    List<Category> tree();
    void save(CategorySaveRequest req);
    void update(Long id, CategorySaveRequest req);
    void delete(Long id);
}