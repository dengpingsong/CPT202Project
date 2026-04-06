package com.cpt202.service.impl;

import com.cpt202.dto.ProjectDTO;
import com.cpt202.dto.ProjectStatusUpdateDTO;
import com.cpt202.model.entity.Project;
import com.cpt202.service.ProjectService;
import com.cpt202.vo.ProjectVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目服务实现类。
 * <p>
 * 负责项目查询、创建、修改和状态流转等业务逻辑的实现。
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    /**
     * 查询学生端项目列表。
     */
    @Override
    public List<ProjectVO> listStudentProjects(String keyword, Long categoryId, Project.ProjectStatus status) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 查询教师端项目列表。
     */
    @Override
    public List<ProjectVO> listTeacherProjects(Long teacherId, Project.ProjectStatus status) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 查询项目详情。
     */
    @Override
    public ProjectVO getProject(Long projectId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 新增项目。
     */
    @Override
    public void create(ProjectDTO projectDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 修改项目。
     */
    @Override
    public void update(Long projectId, ProjectDTO projectDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 修改项目状态。
     */
    @Override
    public void changeStatus(Long projectId, ProjectStatusUpdateDTO projectStatusUpdateDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
