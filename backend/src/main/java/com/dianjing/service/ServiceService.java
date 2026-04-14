package com.dianjing.service;

import com.dianjing.dto.request.ServiceCreateRequest;
import com.dianjing.dto.request.ServiceUpdateRequest;
import com.dianjing.dto.response.ServiceDetailResponse;
import com.dianjing.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ServiceService {

    Service create(Long providerId, ServiceCreateRequest request);

    Service getById(Long id);

    ServiceDetailResponse getDetailById(Long id);

    Service update(Long providerId, Long id, ServiceUpdateRequest request);
    
    Service update(Service service);

    void delete(Long providerId, Long id);

    void toggleStatus(Long providerId, Long id);

    Page<Service> search(String keyword, String gameType, Integer serviceType, Integer status,
                        java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice,
                        String sort, Long providerId, Boolean includeAllStatus, Pageable pageable);

    Page<Service> getByProviderId(Long providerId, Pageable pageable);

    Page<Service> getOnlineServices(String gameType, Integer serviceType, Pageable pageable);

    List<Service> getRecommended(String gameType, Pageable pageable);

    List<Service> getByProviderId(Long providerId);

    long countByProviderId(Long providerId);

    long countOnlineByProviderId(Long providerId);

    long countAll();

    void approveService(Long id);

    void rejectService(Long id);
}
