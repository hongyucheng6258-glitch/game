package com.dianjing.service.impl;

import com.dianjing.service.RateLimitService;
import com.dianjing.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimitServiceImpl implements RateLimitService {

    private static final String RATE_LIMIT_PREFIX = "rate:limit:";
    private static final int DEFAULT_LIMIT = 100;
    private static final int DEFAULT_WINDOW = 60;

    private final RedisUtil redisUtil;

    public RateLimitServiceImpl(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public boolean isAllowed(HttpServletRequest request) {
        String key = getRequestKey(request);
        return isAllowed(key, DEFAULT_LIMIT, DEFAULT_WINDOW);
    }

    @Override
    public boolean isAllowed(String key, int limit, int windowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        Long current = redisUtil.increment(redisKey);
        if (current == 1) {
            redisUtil.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
        }
        return current <= limit;
    }

    @Override
    public void resetLimit(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        redisUtil.delete(redisKey);
    }

    private String getRequestKey(HttpServletRequest request) {
        String ip = getClientIP(request);
        String path = request.getRequestURI();
        return ip + ":" + path;
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
