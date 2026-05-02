package com.cpt202.repository;

import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.ProjectRequest.RequestStatus;
import com.cpt202.vo.ProjectRequestVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRequestRepository extends JpaRepository<ProjectRequest, Long> {

    // 注意这里：通过 student 对象的 studentId 属性查询
    boolean existsByStudent_StudentIdAndRequestStatusIn(Long studentId, List<ProjectRequest.RequestStatus> statuses);

    boolean existsByStudent_StudentIdAndProject_ProjectIdAndRequestStatusIn(
            Long studentId, Long projectId, List<ProjectRequest.RequestStatus> statuses);

    boolean existsByStudent_StudentIdAndPreferenceRankAndRequestStatusIn(
            Long studentId, Integer preferenceRank, List<ProjectRequest.RequestStatus> statuses);

    List<ProjectRequest> findByStudent_StudentIdAndRequestStatus(Long studentId, RequestStatus status);

    long countByProject_ProjectIdAndRequestStatus(Long projectId, RequestStatus status);

    List<ProjectRequest> findByStudent_StudentIdOrderBySubmittedAtDesc(Long studentId);

    List<ProjectRequest> findByProject_Teacher_TeacherIdOrderBySubmittedAtDesc(Long teacherId);

    List<ProjectRequest> findByProject_Teacher_TeacherIdAndRequestStatusOrderBySubmittedAtDesc(
            Long teacherId, RequestStatus status);

    List<ProjectRequest> findByProject_ProjectIdAndRequestStatusIn(Long projectId, List<RequestStatus> statuses);

    List<ProjectRequest> findByRequestStatusOrderBySubmittedAtDesc(RequestStatus status);

    List<ProjectRequest> findAllByOrderBySubmittedAtDesc();

    @Query("""
            select new com.cpt202.vo.ProjectRequestVO(
                pr.requestId,
                p.projectId,
                p.title,
                s.studentId,
                su.fullName,
                t.teacherId,
                pr.preferenceRank,
                pr.notes,
                pr.requestStatus,
                pr.decisionComment,
                pr.submittedAt,
                pr.reviewedAt,
                pr.withdrawnAt
            )
            from ProjectRequest pr
            left join pr.project p
            left join pr.student s
            left join s.user su
            left join pr.reviewedBy t
            where (:status is null or pr.requestStatus = :status)
            order by pr.submittedAt desc
            """)
    List<ProjectRequestVO> findRequestVos(@Param("status") RequestStatus status);
}
