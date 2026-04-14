package com.dianjing.mapper;

import com.dianjing.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelMapper extends JpaRepository<Level, Long> {
    Optional<Level> findByLevel(Integer level);
    
    List<Level> findAllByOrderByLevelAsc();
    
    Optional<Level> findFirstByRequiredExpLessThanEqualOrderByLevelDesc(Integer exp);
}
