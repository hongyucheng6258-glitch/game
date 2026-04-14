package com.dianjing.service;

import com.dianjing.dto.request.ArbitrationRequest;
import com.dianjing.dto.request.ComplaintCreateRequest;
import com.dianjing.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ComplaintService {
    Complaint create(Long userId, ComplaintCreateRequest request);
    Complaint getById(Long id);
    Page<Complaint> getUserComplaints(Long userId, Pageable pageable);
    Page<Complaint> getAllComplaints(Integer status, Pageable pageable);
    Complaint arbitrate(Long arbitratorId, ArbitrationRequest request);
}
