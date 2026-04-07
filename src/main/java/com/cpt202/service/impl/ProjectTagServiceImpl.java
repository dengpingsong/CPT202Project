package com.cpt202.service.impl;

import com.cpt202.service.ProjectTagService;
import com.cpt202.vo.ProjectTagVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目标签服务实现类。
 * <p>
 * 负责查询项目已绑定标签，以及处理项目标签重绑定。
 */
@Service
public class ProjectTagServiceImpl implements ProjectTagService {

    /**
     * 查询项目标签列表。
     */
    @Override
    public List<ProjectTagVO> listProjectTags(Long projectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 绑定项目标签。
     */
    @Override
    public void bindProjectTags(Long projectId, Long teacherId, List<Long> tagIds) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
