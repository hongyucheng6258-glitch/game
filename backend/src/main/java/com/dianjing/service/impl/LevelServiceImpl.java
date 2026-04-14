package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.dto.response.LevelResponse;
import com.dianjing.dto.response.UserLevelInfoResponse;
import com.dianjing.entity.Level;
import com.dianjing.entity.User;
import com.dianjing.mapper.LevelMapper;
import com.dianjing.mapper.UserMapper;
import com.dianjing.service.LevelService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LevelServiceImpl implements LevelService {

    private final LevelMapper levelMapper;
    private final UserMapper userMapper;

    public LevelServiceImpl(LevelMapper levelMapper, UserMapper userMapper) {
        this.levelMapper = levelMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<LevelResponse> getAllLevels() {
        List<Level> levels = levelMapper.findAllByOrderByLevelAsc();
        return levels.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public LevelResponse getLevelByLevel(Integer level) {
        Level levelEntity = levelMapper.findByLevel(level)
                .orElseThrow(() -> new BusinessException(404, "等级不存在"));
        return convertToResponse(levelEntity);
    }

    @Override
    public UserLevelInfoResponse getUserLevelInfo(Long userId) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        
        UserLevelInfoResponse response = new UserLevelInfoResponse();
        
        int experience = user.getExperience() != null ? user.getExperience() : 0;
        BigDecimal totalExpense = user.getTotalExpense() != null ? user.getTotalExpense() : BigDecimal.ZERO;
        Integer currentLevelNum = user.getCurrentLevel() != null ? user.getCurrentLevel() : 1;
        
        // 优先使用用户表中存储的等级，如果不存在则通过经验计算
        Level currentLevel;
        if (currentLevelNum != null && currentLevelNum > 0) {
            currentLevel = levelMapper.findByLevel(currentLevelNum).orElseGet(() -> getCurrentLevel(experience));
        } else {
            currentLevel = getCurrentLevel(experience);
        }
        
        Level nextLevel = levelMapper.findByLevel(currentLevel.getLevel() + 1).orElse(null);
        
        response.setCurrentLevel(currentLevel.getLevel());
        response.setCurrentLevelName(currentLevel.getName());
        response.setCurrentLevelIcon(currentLevel.getIcon());
        response.setExperience(experience);
        response.setCurrentLevelRequiredExp(currentLevel.getRequiredExp());
        response.setTotalExpense(totalExpense);
        
        if (nextLevel != null) {
            response.setNextLevelRequiredExp(nextLevel.getRequiredExp());
            response.setExpToNextLevel(nextLevel.getRequiredExp() - experience);
        } else {
            response.setNextLevelRequiredExp(currentLevel.getRequiredExp());
            response.setExpToNextLevel(0);
        }
        
        response.setDiscountRate(currentLevel.getDiscountRate());
        response.setWithdrawalFeeDiscount(currentLevel.getWithdrawalFeeDiscount());
        response.setDailyWithdrawalLimit(currentLevel.getDailyWithdrawalLimit());
        response.setMaxServicePrice(currentLevel.getMaxServicePrice());
        
        return response;
    }

    @Override
    @Transactional
    public void addExperience(Long userId, Integer exp) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        
        int currentExp = user.getExperience() != null ? user.getExperience() : 0;
        int newExp = currentExp + exp;
        user.setExperience(newExp);
        
        Level newLevel = getCurrentLevel(newExp);
        int userCurrentLevel = user.getCurrentLevel() != null ? user.getCurrentLevel() : 1;
        if (!newLevel.getLevel().equals(userCurrentLevel)) {
            user.setCurrentLevel(newLevel.getLevel());
        }
        
        userMapper.save(user);
    }

    @Override
    public Level getCurrentLevel(Integer exp) {
        if (exp == null || exp < 0) {
            exp = 0;
        }
        return levelMapper.findFirstByRequiredExpLessThanEqualOrderByLevelDesc(exp)
                .orElseGet(() -> levelMapper.findByLevel(1).orElse(null));
    }

    @Override
    @Transactional
    public void initializeDefaultLevels() {
        if (levelMapper.count() > 0) {
            return;
        }
        
        List<Level> levels = new ArrayList<>();
        
        levels.add(createLevel(1, "新手", 0, new BigDecimal("1.00"), new BigDecimal("1.00"), 3, new BigDecimal("1000.00"), "⭐", "新用户初始等级"));
        levels.add(createLevel(2, "青铜", 100, new BigDecimal("0.98"), new BigDecimal("0.95"), 5, new BigDecimal("2000.00"), "🥉", "累计100经验值"));
        levels.add(createLevel(3, "白银", 500, new BigDecimal("0.95"), new BigDecimal("0.90"), 8, new BigDecimal("3000.00"), "🥈", "累计500经验值"));
        levels.add(createLevel(4, "黄金", 1500, new BigDecimal("0.92"), new BigDecimal("0.85"), 10, new BigDecimal("5000.00"), "🥇", "累计1500经验值"));
        levels.add(createLevel(5, "铂金", 4000, new BigDecimal("0.88"), new BigDecimal("0.80"), 15, new BigDecimal("8000.00"), "💎", "累计4000经验值"));
        levels.add(createLevel(6, "钻石", 10000, new BigDecimal("0.85"), new BigDecimal("0.75"), 20, new BigDecimal("12000.00"), "💠", "累计10000经验值"));
        levels.add(createLevel(7, "大师", 25000, new BigDecimal("0.80"), new BigDecimal("0.70"), 30, new BigDecimal("20000.00"), "🏆", "累计25000经验值"));
        levels.add(createLevel(8, "王者", 60000, new BigDecimal("0.75"), new BigDecimal("0.60"), 50, new BigDecimal("50000.00"), "👑", "累计60000经验值"));
        levels.add(createLevel(9, "传奇", 150000, new BigDecimal("0.70"), new BigDecimal("0.50"), 100, new BigDecimal("100000.00"), "🌟", "累计150000经验值"));
        levels.add(createLevel(10, "至尊", 500000, new BigDecimal("0.65"), new BigDecimal("0.40"), 200, new BigDecimal("200000.00"), "🎖️", "累计500000经验值"));
        
        levelMapper.saveAll(levels);
    }

    private Level createLevel(int level, String name, int requiredExp, BigDecimal discountRate, 
                             BigDecimal withdrawalFeeDiscount, int dailyWithdrawalLimit, 
                             BigDecimal maxServicePrice, String icon, String description) {
        Level l = new Level();
        l.setLevel(level);
        l.setName(name);
        l.setRequiredExp(requiredExp);
        l.setDiscountRate(discountRate);
        l.setWithdrawalFeeDiscount(withdrawalFeeDiscount);
        l.setDailyWithdrawalLimit(dailyWithdrawalLimit);
        l.setMaxServicePrice(maxServicePrice);
        l.setIcon(icon);
        l.setDescription(description);
        return l;
    }

    private LevelResponse convertToResponse(Level level) {
        LevelResponse response = new LevelResponse();
        BeanUtils.copyProperties(level, response);
        return response;
    }
}
