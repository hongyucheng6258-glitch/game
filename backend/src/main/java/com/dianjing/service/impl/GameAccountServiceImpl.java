package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.dto.request.GameAccountRequest;
import com.dianjing.entity.GameAccount;
import com.dianjing.mapper.GameAccountMapper;
import com.dianjing.service.GameAccountService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GameAccountServiceImpl implements GameAccountService {

    private final GameAccountMapper gameAccountMapper;

    public GameAccountServiceImpl(GameAccountMapper gameAccountMapper) {
        this.gameAccountMapper = gameAccountMapper;
    }

    @Override
    public GameAccount create(Long userId, GameAccountRequest request) {
        GameAccount account = new GameAccount();
        account.setUserId(userId);
        account.setGameType(request.getGameType());
        account.setAccountName(request.getAccountName());
        account.setAccountPassword(request.getAccountPassword());
        account.setAccountLevel(request.getAccountLevel());
        account.setRegion(request.getRegion());
        account.setAccountDescription(request.getAccountDescription());
        return gameAccountMapper.save(account);
    }

    @Override
    public List<GameAccount> getByUserId(Long userId) {
        return gameAccountMapper.findByUserIdOrderByIdDesc(userId);
    }

    @Override
    public GameAccount getById(Long userId, Long id) {
        GameAccount account = gameAccountMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "游戏账号不存在"));
        if (!account.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此账号");
        }
        return account;
    }

    @Override
    public GameAccount update(Long userId, Long id, GameAccountRequest request) {
        GameAccount account = gameAccountMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "游戏账号不存在"));
        if (!account.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此账号");
        }
        if (request.getGameType() != null) account.setGameType(request.getGameType());
        if (request.getAccountName() != null) account.setAccountName(request.getAccountName());
        if (request.getAccountPassword() != null) account.setAccountPassword(request.getAccountPassword());
        if (request.getAccountLevel() != null) account.setAccountLevel(request.getAccountLevel());
        if (request.getRegion() != null) account.setRegion(request.getRegion());
        if (request.getAccountDescription() != null) account.setAccountDescription(request.getAccountDescription());
        return gameAccountMapper.save(account);
    }

    @Override
    public void delete(Long userId, Long id) {
        gameAccountMapper.findById(id).ifPresent(account -> {
            if (account.getUserId().equals(userId)) {
                gameAccountMapper.delete(account);
            }
        });
    }
}
