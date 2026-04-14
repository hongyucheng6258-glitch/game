package com.dianjing.service;

import com.dianjing.dto.response.LevelResponse;
import com.dianjing.dto.response.UserLevelInfoResponse;
import com.dianjing.entity.Level;

import java.util.List;

public interface LevelService {
    List<LevelResponse> getAllLevels();
    
    LevelResponse getLevelByLevel(Integer level);
    
    UserLevelInfoResponse getUserLevelInfo(Long userId);
    
    void addExperience(Long userId, Integer exp);
    
    Level getCurrentLevel(Integer exp);
    
    void initializeDefaultLevels();
}
