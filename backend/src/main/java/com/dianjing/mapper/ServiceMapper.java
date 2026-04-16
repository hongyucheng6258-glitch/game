package com.dianjing.mapper;

import com.dianjing.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ServiceMapper extends JpaRepository<Service, Long>, JpaSpecificationExecutor<Service> {
    List<Service> findByProviderIdOrderByIdDesc(Long providerId);
    List<Service> findByProviderId(Long providerId);
    Page<Service> findByProviderIdOrderByIdDesc(Long providerId, Pageable pageable);
    Page<Service> findByStatus(Integer status, Pageable pageable);
    long countByProviderId(Long providerId);
    long countByProviderIdAndStatus(Long providerId, Integer status);

    @Query("SELECT s FROM Service s WHERE s.status = 1 " +
           "AND (:gameType IS NULL OR s.gameType = :gameType) " +
           "AND (:serviceType IS NULL OR s.serviceType = :serviceType) " +
           "ORDER BY s.rating DESC, s.salesCount DESC")
    Page<Service> findServices(@Param("gameType") String gameType,
                               @Param("serviceType") Integer serviceType,
                               Pageable pageable);
}
