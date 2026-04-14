package com.dianjing.service.impl;

import com.dianjing.service.LoginAttemptService;
import com.dianjing.util.RedisUtil;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final String LOGIN_ATTEMPT_PREFIX = "login:attempt:";
    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_DURATION = 15;

    private final RedisUtil redisUtil;

    public LoginAttemptServiceImpl(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public void registerFailedAttempt(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        Long attempts = redisUtil.increment(key);
        if (attempts == 1) {
            redisUtil.expire(key, BLOCK_DURATION, TimeUnit.MINUTES);
        }
    }

    @Override
    public boolean isBlocked(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        String attemptsStr = redisUtil.get(key);
        if (attemptsStr == null) {
            return false;
        }
        try {
            int attempts = Integer.parseInt(attemptsStr);
            return attempts >= MAX_ATTEMPTS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void resetAttempts(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        redisUtil.delete(key);
    }

    @Override
    public int getAttempts(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        String attemptsStr = redisUtil.get(key);
        if (attemptsStr == null) {
            return 0;
        }
        try {
            return Integer.parseInt(attemptsStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
