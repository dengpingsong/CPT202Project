package com.cpt202.repository;

import com.cpt202.model.entity.Project;
import com.cpt202.repository.specification.ProjectSpecifications;
import com.cpt202.vo.ProjectVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    List<Project> findByTeacher_TeacherIdOrderByCreatedAtDesc(Long teacherId);

    List<Project> findByTeacher_TeacherIdAndProjectStatusOrderByCreatedAtDesc(
            Long teacherId, Project.ProjectStatus status);

    default Page<Project> findStudentProjects(String keyword,
                                              Long categoryId,
                                              Project.ProjectStatus status,
                                              List<Long> tagIds,
                                              Pageable pageable) {
        return findAll(ProjectSpecifications.studentQuery(keyword, categoryId, status, tagIds), pageable);
    }

    @Query("""
            select new com.cpt202.vo.ProjectVO(
                p.projectId,
                t.teacherId,
                tu.fullName,
                c.categoryId,
                c.categoryName,
                p.title,
                p.description,
                p.requiredSkills,
                p.topicArea,
                p.maxStudents,
                p.currentAgreedCount,
                p.projectStatus,
                p.publishDate,
                p.closeDate
            )
            from Project p
            left join p.teacher t
            left join t.user tu
            left join p.category c
            order by p.createdAt desc
            """)
    List<ProjectVO> findAllProjectVos();

    @Query(value = """
            select new com.cpt202.vo.ProjectVO(
                p.projectId,
                t.teacherId,
                tu.fullName,
                c.categoryId,
                c.categoryName,
                p.title,
                p.description,
                p.requiredSkills,
                p.topicArea,
                p.maxStudents,
                p.currentAgreedCount,
                p.projectStatus,
                p.publishDate,
                p.closeDate
            )
            from Project p
            left join p.teacher t
            left join t.user tu
            left join p.category c
            order by p.createdAt desc
            """,
            countQuery = "select count(p) from Project p")
    Page<ProjectVO> findAllProjectVos(Pageable pageable);

        @Query(value = """
                        select new com.cpt202.vo.ProjectVO(
                                p.projectId,
                                t.teacherId,
                                tu.fullName,
                                c.categoryId,
                                c.categoryName,
                                p.title,
                                p.description,
                                p.requiredSkills,
                                p.topicArea,
                                p.maxStudents,
                                p.currentAgreedCount,
                                p.projectStatus,
                                p.publishDate,
                                p.closeDate
                        )
                        from Project p
                        left join p.teacher t
                        left join t.user tu
                        left join p.category c
                        where t.teacherId = :teacherId
                            and (:status is null or p.projectStatus = :status)
                        order by p.createdAt desc
                        """,
                        countQuery = """
                        select count(p)
                        from Project p
                        where p.teacher.teacherId = :teacherId
                            and (:status is null or p.projectStatus = :status)
                        """)
        Page<ProjectVO> findTeacherProjectVos(@Param("teacherId") Long teacherId,
                                                                                    @Param("status") Project.ProjectStatus status,
                                                                                    Pageable pageable);
}
