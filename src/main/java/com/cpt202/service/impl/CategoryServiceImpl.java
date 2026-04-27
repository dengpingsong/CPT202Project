package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.dto.CategoryDTO;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.Category;
import com.cpt202.repository.CategoryRepository;
import com.cpt202.service.CategoryService;
import com.cpt202.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类管理服务实现类。
 * <p>
 * 当前处于接口骨架阶段，具体持久化逻辑后续补充。
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 查询全部分类列表。
     */
    @Override
    public List<CategoryVO> listAll() {
        List<Category> categories = categoryRepository.findAll();

        List<CategoryVO> categoryVos = new ArrayList<>(categories.size());
        for (Category category : categories) {
            categoryVos.add(toCategoryVO(category));
        }
        return categoryVos;
    }

    /**
     * 根据分类主键查询分类详情。
     */
    @Override
    public CategoryVO getById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.CATEGORY_NOT_FOUND));
        return toCategoryVO(category);
    }

    /**
     * 新增分类。
     *
     * @param categoryDTO 分类新增参数
     */
    @Override
    public void create(CategoryDTO categoryDTO) {
        LocalDateTime now = LocalDateTime.now();

        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        categoryRepository.save(category);
    }

    /**
     * 修改分类。
     *
     * @param categoryId 分类主键
     * @param categoryDTO 分类更新参数
     */
    @Override
    public void update(Long categoryId, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.CATEGORY_TO_UPDATE_NOT_FOUND));

        category.setCategoryName(categoryDTO.getCategoryName());
        category.setDescription(categoryDTO.getDescription());
        category.setUpdatedAt(LocalDateTime.now());

        categoryRepository.save(category);
    }

    /**
     * 删除分类。
     *
     * @param categoryId 分类主键
     */
    @Override
    public void delete(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException(MessageConstants.CATEGORY_TO_DELETE_NOT_FOUND);
        }

        categoryRepository.deleteById(categoryId);
    }

    private CategoryVO toCategoryVO(Category category) {
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);

        return categoryVO;
    }
}
