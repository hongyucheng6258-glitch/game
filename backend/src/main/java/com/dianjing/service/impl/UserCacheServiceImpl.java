package com.dianjing.service.impl;

import com.dianjing.entity.User;
import com.dianjing.service.UserCacheService;
import com.dianjing.util.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Component
public class UserCacheServiceImpl implements UserCacheService {

    private static final String USER_PREFIX = "user:info:";
    private static final String USER_USERNAME_PREFIX = "user:username:";
    private static final String USER_BALANCE_PREFIX = "user:balance:";
    private static final long USER_TTL = 30;
    private static final long BALANCE_TTL = 15;

    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    public UserCacheServiceImpl(RedisUtil redisUtil, ObjectMapper objectMapper) {
        this.redisUtil = redisUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void cacheUser(User user) {
        try {
            String userKey = USER_PREFIX + user.getId();
            String usernameKey = USER_USERNAME_PREFIX + user.getUsername();
            String json = objectMapper.writeValueAsString(user);
            redisUtil.set(userKey, json, USER_TTL, TimeUnit.MINUTES);
            redisUtil.set(usernameKey, json, USER_TTL, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize user", e);
        }
    }

    @Override
    public User getCachedUser(Long userId) {
        String key = USER_PREFIX + userId;
        String json = redisUtil.get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize user", e);
        }
    }

    @Override
    public User getCachedUserByUsername(String username) {
        String key = USER_USERNAME_PREFIX + username;
        String json = redisUtil.get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize user", e);
        }
    }

    @Override
    public void evictUser(Long userId) {
        String key = USER_PREFIX + userId;
        redisUtil.delete(key);
    }

    @Override
    public void evictUserByUsername(String username) {
        String key = USER_USERNAME_PREFIX + username;
        redisUtil.delete(key);
    }

    @Override
    public void cacheUserBalance(Long userId, BigDecimal balance) {
        String key = USER_BALANCE_PREFIX + userId;
        redisUtil.set(key, balance.toString(), BALANCE_TTL, TimeUnit.MINUTES);
    }

    @Override
    public BigDecimal getCachedUserBalance(Long userId) {
        String key = USER_BALANCE_PREFIX + userId;
        String value = redisUtil.get(key);
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void evictUserBalance(Long userId) {
        String key = USER_BALANCE_PREFIX + userId;
        redisUtil.delete(key);
    }

    @Override
    public void evictAllUserCaches() {
        redisUtil.keys(USER_PREFIX + "*").forEach(redisUtil::delete);
        redisUtil.keys(USER_USERNAME_PREFIX + "*").forEach(redisUtil::delete);
        redisUtil.keys(USER_BALANCE_PREFIX + "*").forEach(redisUtil::delete);
    }
}
