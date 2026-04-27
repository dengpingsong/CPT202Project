package com.cpt202.repository;

import com.cpt202.model.entity.RequestStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestStatusHistoryRepository extends JpaRepository<RequestStatusHistory, Long> {

    List<RequestStatusHistory> findByRequest_RequestIdOrderByChangedAtAsc(Long requestId);

    List<RequestStatusHistory> findAllByOrderByChangedAtAsc();
}
