package com.dianjing.service;

public interface TokenCacheService {
    void addToBlacklist(String token, long expirationMillis);
    boolean isBlacklisted(String token);
    void removeFromBlacklist(String token);
}
