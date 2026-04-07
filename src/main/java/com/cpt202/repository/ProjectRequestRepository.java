package com.cpt202.repository;

import com.cpt202.model.entity.ProjectRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRequestRepository extends JpaRepository<ProjectRequest, Long> {

}
