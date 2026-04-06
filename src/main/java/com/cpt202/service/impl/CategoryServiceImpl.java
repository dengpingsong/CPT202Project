package com.cpt202.service.impl;

import com.cpt202.dto.CategoryDTO;
import com.cpt202.service.CategoryService;
import com.cpt202.vo.CategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类管理服务实现类。
 * <p>
 * 当前处于接口骨架阶段，具体持久化逻辑后续补充。
 */
@Service
public class CategoryServiceImpl implements CategoryService {

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
