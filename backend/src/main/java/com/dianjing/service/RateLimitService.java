package com.dianjing.service;

import jakarta.servlet.http.HttpServletRequest;

public interface RateLimitService {
    boolean isAllowed(HttpServletRequest request);
    boolean isAllowed(String key, int limit, int windowSeconds);
    void resetLimit(String key);
}
