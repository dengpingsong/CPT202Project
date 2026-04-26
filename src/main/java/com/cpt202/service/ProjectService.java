package com.cpt202.service;

import com.cpt202.dto.ProjectDTO;
import com.cpt202.dto.ProjectStatusUpdateDTO;
import com.cpt202.dto.StudentProjectQueryDTO;
import com.cpt202.model.entity.Project;
import com.cpt202.result.PageResult;
import com.cpt202.vo.ProjectVO;

import java.util.List;

/**
 * 项目服务接口。
 * 负责学生端与教师端项目查询，以及教师端项目维护操作。
 */
public interface ProjectService {

    /**
     * 学生端项目分页查询。
     *
     * @param queryDTO 学生端项目查询参数（关键字 / 分类 / 状态 / 标签 / 分页）
     * @return 分页项目展示对象
     */
    PageResult<ProjectVO> listStudentProjects(StudentProjectQueryDTO queryDTO);

    /**
     * 教师端查询本人项目列表。
     *
     * @param teacherId 教师主键
     * @param status 项目状态
     * @return 项目展示对象列表
     */
    List<ProjectVO> listTeacherProjects(Long teacherId, Project.ProjectStatus status);

    /**
     * 查询项目详情。
     *
     * @param projectId 项目主键
     * @return 项目展示对象
     */
    ProjectVO getProject(Long projectId);

    /**
     * 新增项目。
     *
     * @param projectDTO 项目新增参数
     */
    void create(ProjectDTO projectDTO);

    /**
     * 修改项目。
     *
     * @param projectId 项目主键
     * @param projectDTO 项目更新参数
     */
    void update(Long projectId, ProjectDTO projectDTO);

    /**
     * 修改项目状态。
     *
     * @param projectId 项目主键
     * @param projectStatusUpdateDTO 项目状态修改参数
     */
    void changeStatus(Long projectId, ProjectStatusUpdateDTO projectStatusUpdateDTO);
}
