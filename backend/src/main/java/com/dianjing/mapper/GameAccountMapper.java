package com.dianjing.mapper;

import com.dianjing.entity.GameAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameAccountMapper extends JpaRepository<GameAccount, Long> {
    List<GameAccount> findByUserIdOrderByIdDesc(Long userId);
    List<GameAccount> findByUserIdAndGameType(Long userId, String gameType);
    void deleteByIdAndUserId(Long id, Long userId);
}
