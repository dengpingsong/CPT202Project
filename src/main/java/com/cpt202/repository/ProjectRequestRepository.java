package com.cpt202.repository;

import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.ProjectRequest.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRequestRepository extends JpaRepository<ProjectRequest, Long> {

    // 注意这里：通过 student 对象的 studentId 属性查询
    boolean existsByStudent_StudentIdAndRequestStatusIn(Long studentId, List<ProjectRequest.RequestStatus> statuses);

    List<ProjectRequest> findByStudent_StudentIdAndRequestStatus(Long studentId, RequestStatus status);

    long countByProject_ProjectIdAndRequestStatus(Long projectId, RequestStatus status);

    List<ProjectRequest> findByStudent_StudentIdOrderBySubmittedAtDesc(Long studentId);

    List<ProjectRequest> findByProject_Teacher_TeacherIdOrderBySubmittedAtDesc(Long teacherId);

    List<ProjectRequest> findByProject_Teacher_TeacherIdAndRequestStatusOrderBySubmittedAtDesc(
            Long teacherId, RequestStatus status);

    List<ProjectRequest> findByRequestStatusOrderBySubmittedAtDesc(RequestStatus status);

    List<ProjectRequest> findAllByOrderBySubmittedAtDesc();
}
