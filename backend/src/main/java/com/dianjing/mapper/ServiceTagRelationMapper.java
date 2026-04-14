package com.dianjing.mapper;

import com.dianjing.entity.ServiceTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceTagRelationMapper extends JpaRepository<ServiceTagRelation, Long> {
    List<ServiceTagRelation> findByServiceId(Long serviceId);
    void deleteByServiceId(Long serviceId);
    void deleteByServiceIdAndTagId(Long serviceId, Long tagId);
    boolean existsByServiceIdAndTagId(Long serviceId, Long tagId);
    long countByTagId(Long tagId);
}
