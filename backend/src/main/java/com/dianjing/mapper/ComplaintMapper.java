package com.dianjing.mapper;

import com.dianjing.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ComplaintMapper extends JpaRepository<Complaint, Long> {
    Page<Complaint> findByComplainantIdOrderByIdDesc(Long complainantId, Pageable pageable);
    Page<Complaint> findByRespondentIdOrderByIdDesc(Long respondentId, Pageable pageable);
    Page<Complaint> findAllByOrderByIdDesc(Pageable pageable);
    Page<Complaint> findByStatusOrderByIdDesc(Integer status, Pageable pageable);
    Page<Complaint> findByOrderId(Long orderId, Pageable pageable);
    
    @Query("SELECT COUNT(c) > 0 FROM Complaint c WHERE c.orderId = :orderId AND c.status NOT IN (:resolvedStatuses)")
    boolean existsPendingComplaint(@Param("orderId") Long orderId, @Param("resolvedStatuses") List<Integer> resolvedStatuses);
}
