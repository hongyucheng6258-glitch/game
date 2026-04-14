package com.dianjing.mapper;

import com.dianjing.entity.ServiceTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ServiceTagMapper extends JpaRepository<ServiceTag, Long> {
    Optional<ServiceTag> findByName(String name);
    boolean existsByName(String name);
    List<ServiceTag> findAllByOrderByNameAsc();
}
