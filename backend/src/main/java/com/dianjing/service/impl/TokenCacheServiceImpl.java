package com.dianjing.service.impl;

import com.dianjing.service.TokenCacheService;
import com.dianjing.util.RedisUtil;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenCacheServiceImpl implements TokenCacheService {

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    private final RedisUtil redisUtil;

    public TokenCacheServiceImpl(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public void addToBlacklist(String token, long expirationMillis) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        redisUtil.set(key, "1", expirationMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return redisUtil.hasKey(key);
    }

    @Override
    public void removeFromBlacklist(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        redisUtil.delete(key);
    }
}
