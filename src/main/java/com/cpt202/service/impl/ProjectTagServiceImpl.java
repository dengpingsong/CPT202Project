package com.cpt202.service.impl;

import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectTag;
import com.cpt202.model.entity.ProjectTagId;
import com.cpt202.model.entity.Tag;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectTagRepository;
import com.cpt202.repository.TagRepository;
import com.cpt202.service.ProjectTagService;
import com.cpt202.vo.ProjectTagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目标签服务实现类。
 * <p>
 * 负责查询项目已绑定标签，以及处理项目标签重绑定。
 */
@Service
@RequiredArgsConstructor
public class ProjectTagServiceImpl implements ProjectTagService {

    private final ProjectTagRepository projectTagRepository;
    private final ProjectRepository projectRepository;
    private final TagRepository tagRepository;

    /**
     * 查询项目标签列表。
     */
    @Override
    public List<ProjectTagVO> listProjectTags(Long projectId) {
        return projectTagRepository.findByProject_ProjectIdOrderByTag_TagIdAsc(projectId)
                .stream()
                .map(projectTag -> ProjectTagVO.builder()
                        .projectId(projectTag.getProject().getProjectId())
                        .tagId(projectTag.getTag().getTagId())
                        .tagName(projectTag.getTag().getTagName())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 绑定项目标签。
     */
    @Override
    @Transactional
    public void bindProjectTags(Long projectId, Long teacherId, List<Long> tagIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("项目不存在。"));
        if (project.getTeacher() == null || !teacherId.equals(project.getTeacher().getTeacherId())) {
            throw new BusinessException("不能修改其他教师名下项目的标签。");
        }

        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new NotFoundException("部分标签不存在。");
        }

        projectTagRepository.deleteByProject_ProjectId(projectId);
        List<ProjectTag> projectTags = tags.stream()
                .map(tag -> ProjectTag.builder()
                        .id(new ProjectTagId(projectId, tag.getTagId()))
                        .project(project)
                        .tag(tag)
                        .build())
                .toList();
        projectTagRepository.saveAll(projectTags);
    }
}
