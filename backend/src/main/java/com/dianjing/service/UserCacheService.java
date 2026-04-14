package com.dianjing.service;

import com.dianjing.entity.User;

import java.math.BigDecimal;

public interface UserCacheService {
    void cacheUser(User user);
    User getCachedUser(Long userId);
    User getCachedUserByUsername(String username);
    void evictUser(Long userId);
    void evictUserByUsername(String username);
    
    void cacheUserBalance(Long userId, BigDecimal balance);
    BigDecimal getCachedUserBalance(Long userId);
    void evictUserBalance(Long userId);
    
    void evictAllUserCaches();
}
