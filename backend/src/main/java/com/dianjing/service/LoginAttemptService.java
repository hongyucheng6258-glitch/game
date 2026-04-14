package com.dianjing.service;

public interface LoginAttemptService {
    void registerFailedAttempt(String username);
    boolean isBlocked(String username);
    void resetAttempts(String username);
    int getAttempts(String username);
}
