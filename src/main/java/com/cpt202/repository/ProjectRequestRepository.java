package com.cpt202.repository;

import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.ProjectRequest.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRequestRepository extends JpaRepository<ProjectRequest, Long> {

}
