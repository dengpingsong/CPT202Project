package com.cpt202.repository;

import com.cpt202.model.entity.RequestStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestStatusHistoryRepository extends JpaRepository<RequestStatusHistory, Long> {
}
