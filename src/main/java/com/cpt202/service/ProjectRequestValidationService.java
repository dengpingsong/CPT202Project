package com.cpt202.service;

public interface ProjectRequestValidationService {
    void validateRequest(Long studentId);
    void onApprovalSuccess(Long requestId);
}