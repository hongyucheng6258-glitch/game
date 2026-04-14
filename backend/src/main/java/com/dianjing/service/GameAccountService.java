package com.dianjing.service;

import com.dianjing.dto.request.GameAccountRequest;
import com.dianjing.entity.GameAccount;

import java.util.List;

public interface GameAccountService {

    GameAccount create(Long userId, GameAccountRequest request);

    List<GameAccount> getByUserId(Long userId);

    GameAccount getById(Long userId, Long id);

    GameAccount update(Long userId, Long id, GameAccountRequest request);

    void delete(Long userId, Long id);
}
