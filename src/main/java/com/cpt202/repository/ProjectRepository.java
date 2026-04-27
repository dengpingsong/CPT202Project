package com.cpt202.repository;

import com.cpt202.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    List<Project> findByTeacher_TeacherIdOrderByCreatedAtDesc(Long teacherId);

    List<Project> findByTeacher_TeacherIdAndProjectStatusOrderByCreatedAtDesc(
            Long teacherId, Project.ProjectStatus status);
}
