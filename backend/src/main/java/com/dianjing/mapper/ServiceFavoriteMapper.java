package com.dianjing.mapper;

import com.dianjing.entity.ServiceFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ServiceFavoriteMapper extends JpaRepository<ServiceFavorite, Long> {
    Page<ServiceFavorite> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);
    Optional<ServiceFavorite> findByUserIdAndServiceId(Long userId, Long serviceId);
    boolean existsByUserIdAndServiceId(Long userId, Long serviceId);
    void deleteByUserIdAndServiceId(Long userId, Long serviceId);
    long countByUserId(Long userId);
}
