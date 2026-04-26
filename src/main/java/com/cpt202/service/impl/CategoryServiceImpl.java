package com.cpt202.service.impl;

import com.cpt202.dto.CategoryDTO;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.Category;
import com.cpt202.repository.CategoryRepository;
import com.cpt202.service.CategoryService;
import com.cpt202.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryVO> listAll() {
        return categoryRepository.findAll().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryVO getById(Long categoryId) {
        return toVO(categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("分类不存在。")));
    }

    @Override
    @Transactional
    public void create(CategoryDTO categoryDTO) {
        Category category = Category.builder()
                .categoryName(categoryDTO.getCategoryName())
                .description(categoryDTO.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void update(Long categoryId, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("分类不存在。"));
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setDescription(categoryDTO.getDescription());
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void delete(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("分类不存在。");
        }
        categoryRepository.deleteById(categoryId);
    }

    private CategoryVO toVO(Category c) {
        return CategoryVO.builder()
                .categoryId(c.getCategoryId())
                .categoryName(c.getCategoryName())
                .description(c.getDescription())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}

    /**
     * 查询全部分类列表。
     *
     * @return 分类展示对象列表
     */
    @Override
    public List<CategoryVO> listAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 根据分类主键查询分类详情。
     *
     * @param categoryId 分类主键
     * @return 分类展示对象
     */
    @Override
    public CategoryVO getById(Long categoryId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 新增分类。
     *
     * @param categoryDTO 分类新增参数
     */
    @Override
    public void create(CategoryDTO categoryDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 修改分类。
     *
     * @param categoryId 分类主键
     * @param categoryDTO 分类更新参数
     */
    @Override
    public void update(Long categoryId, CategoryDTO categoryDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 删除分类。
     *
     * @param categoryId 分类主键
     */
    @Override
    public void delete(Long categoryId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
