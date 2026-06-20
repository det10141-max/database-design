package com.library.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.BusinessException;
import com.library.dto.request.CategorySaveRequest;
import com.library.entity.Category;
import com.library.mapper.BookMapper;
import com.library.mapper.CategoryMapper;
import com.library.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;
    private final BookMapper bookMapper;

    @Override
    public List<Category> tree() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));
    }

    @Override
    public void save(CategorySaveRequest req) {
        Category c = new Category();
        c.setName(req.getName());
        c.setParentId(req.getParentId() != null ? req.getParentId() : 0L);
        c.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        categoryMapper.insert(c);
    }

    @Override
    public void update(Long id, CategorySaveRequest req) {
        Category c = categoryMapper.selectById(id);
        if (c == null) throw new BusinessException("分类不存在");
        c.setName(req.getName());
        if (req.getParentId() != null) c.setParentId(req.getParentId());
        if (req.getSortOrder() != null) c.setSortOrder(req.getSortOrder());
        categoryMapper.updateById(c);
    }

    @Override
    public void delete(Long id) {
        if (categoryMapper.selectCount(new LambdaQueryWrapper<Category>().eq(Category::getParentId, id)) > 0)
            throw new BusinessException("该分类下有子分类，不能删除");
        if (bookMapper.selectCount(new LambdaQueryWrapper<com.library.entity.Book>().eq(com.library.entity.Book::getCategoryId, id)) > 0)
            throw new BusinessException("该分类下有图书，不能删除");
        categoryMapper.deleteById(id);
    }
}
