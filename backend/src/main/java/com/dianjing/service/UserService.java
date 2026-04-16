package com.dianjing.service;

import com.dianjing.dto.request.*;
import com.dianjing.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

public interface UserService {

    User register(RegisterRequest request);

    String login(LoginRequest request);

    String phoneLogin(PhoneLoginRequest request);

    String sendSmsCode(String phone);

    User getUserById(Long id);

    User getUserByUsername(String username);

    User getUserByPhone(String phone);

    User updateUserInfo(Long userId, UserUpdateRequest request);

    void updatePassword(Long userId, PasswordUpdateRequest request);

    String updateAvatar(Long userId, MultipartFile file);

    BigDecimal getBalance(Long userId);

    void updateRole(Long userId, Integer role);

    void updateStatus(Long userId, Integer status);

    Page<User> getAllUsers(String keyword, Integer role, Integer status, Pageable pageable);

    long countUsers();

    long countProviders();

    User adminUpdateUser(Long userId, UserUpdateRequest request);

    void adminResetPassword(Long userId, String newPassword);

    void adminAdjustBalance(Long userId, BigDecimal amount, String remark);

    void resetPassword(String phone, String code, String newPassword);

    boolean existsByUsername(String username);

    Map<String, Object> getUserOrderStats(Long userId);
}
