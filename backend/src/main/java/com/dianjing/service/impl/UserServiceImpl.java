package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.common.Constants;
import com.dianjing.dto.request.*;
import com.dianjing.entity.User;
import com.dianjing.mapper.OrderMapper;
import com.dianjing.mapper.UserMapper;
import com.dianjing.security.JwtTokenProvider;
import com.dianjing.service.LoginAttemptService;
import com.dianjing.service.UserCacheService;
import com.dianjing.service.UserService;
import com.dianjing.util.FileUtil;
import com.dianjing.util.PasswordUtil;
import com.dianjing.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.dianjing.service.SystemSettingService systemSettingService;
    private final UserCacheService userCacheService;
    private final LoginAttemptService loginAttemptService;
    private final RedisUtil redisUtil;
    private final Random random = new Random();
    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    public UserServiceImpl(UserMapper userMapper, OrderMapper orderMapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
                           com.dianjing.service.SystemSettingService systemSettingService, UserCacheService userCacheService,
                           LoginAttemptService loginAttemptService, RedisUtil redisUtil) {
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.systemSettingService = systemSettingService;
        this.userCacheService = userCacheService;
        this.loginAttemptService = loginAttemptService;
        this.redisUtil = redisUtil;
    }

    @Override
    public User register(RegisterRequest request) {
        Boolean allowRegister = systemSettingService.getBoolean("allowRegister", true);
        if (!allowRegister) {
            throw new BusinessException(400, "当前不允许注册新用户");
        }
        
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty() && userMapper.existsByPhone(request.getPhone())) {
            throw new BusinessException(400, "手机号已被注册");
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty() && userMapper.existsByEmail(request.getEmail())) {
            throw new BusinessException(400, "邮箱已被注册");
        }

        if (!PasswordUtil.isValidPassword(request.getPassword())) {
            throw new BusinessException(400, "密码强度不足，需要包含至少两种字符类型（字母、数字、特殊符号）");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        
        Integer defaultRole = systemSettingService.getInt("defaultRole", Constants.ROLE_USER);
        user.setRole(defaultRole);
        
        user.setStatus(Constants.STATUS_NORMAL);
        user.setBalance(BigDecimal.ZERO);
        return userMapper.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }

    @Override
    public Map<String, Object> getUserOrderStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        long totalOrders = orderMapper.countByUserId(userId);
        long pendingPayment = orderMapper.countByUserIdAndStatus(userId, Constants.ORDER_PENDING_PAYMENT);
        long pendingService = orderMapper.countByUserIdAndStatus(userId, Constants.ORDER_PENDING_SERVICE);
        long inService = orderMapper.countByUserIdAndStatus(userId, Constants.ORDER_IN_SERVICE);
        long pendingReview = orderMapper.countByUserIdAndStatus(userId, Constants.ORDER_PENDING_REVIEW);
        long completed = orderMapper.countByUserIdAndStatus(userId, Constants.ORDER_COMPLETED);
        long cancelled = orderMapper.countByUserIdAndStatus(userId, Constants.ORDER_CANCELLED);

        stats.put("totalOrders", totalOrders);
        stats.put("pendingPayment", pendingPayment);
        stats.put("pendingService", pendingService);
        stats.put("inService", inService);
        stats.put("pendingReview", pendingReview);
        stats.put("completed", completed);
        stats.put("cancelled", cancelled);
        return stats;
    }

    @Override
    public String login(LoginRequest request) {
        String username = request.getUsername();
        
        // 检查是否被锁定
        if (loginAttemptService.isBlocked(username)) {
            throw new BusinessException(423, "登录失败次数过多，请15分钟后再试");
        }
        
        User user = userMapper.findByUsername(username)
                .orElseThrow(() -> {
                    loginAttemptService.registerFailedAttempt(username);
                    throw new BusinessException(401, "用户名或密码错误");
                });
        
        if (user.getStatus() == Constants.STATUS_DISABLED) {
            throw new BusinessException(403, "账号已被禁用");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.registerFailedAttempt(username);
            int attempts = loginAttemptService.getAttempts(username);
            throw new BusinessException(401, "用户名或密码错误 (" + attempts + "/5)");
        }
        
        // 登录成功，重置失败次数
        loginAttemptService.resetAttempts(username);
        
        // 缓存用户信息
        userCacheService.cacheUser(user);
        
        return jwtTokenProvider.generateTokenByUsername(user.getUsername());
    }

    @Override
    public String sendSmsCode(String phone) {
        String redisKey = "sms:code:" + phone;
        
        if (redisUtil.hasKey(redisKey)) {
            Long expire = redisUtil.getExpire(redisKey);
            if (expire != null && expire > 55) {
                throw new BusinessException(400, "验证码发送太频繁，请稍后再试");
            }
        }
        
        String code = String.format("%06d", random.nextInt(1000000));
        redisUtil.set(redisKey, code, 5, TimeUnit.MINUTES);
        
        System.out.println("手机号 " + phone + " 的验证码是: " + code);
        return code;
    }

    @Override
    public String phoneLogin(PhoneLoginRequest request) {
        String phone = request.getPhone();
        String code = request.getCode();
        
        String redisKey = "sms:code:" + phone;
        String storedCode = redisUtil.get(redisKey);
        
        if (storedCode == null) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }
        
        if (!storedCode.equals(code)) {
            throw new BusinessException(400, "验证码错误");
        }
        
        User user = userMapper.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(404, "该手机号未注册"));
        
        if (user.getStatus() == Constants.STATUS_DISABLED) {
            throw new BusinessException(403, "账号已被禁用");
        }
        
        redisUtil.delete(redisKey);
        userCacheService.cacheUser(user);
        
        return jwtTokenProvider.generateTokenByUsername(user.getUsername());
    }

    @Override
    public User getUserByPhone(String phone) {
        User user = userMapper.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return user;
    }

    @Override
    public User getUserById(Long id) {
        // 直接从数据库获取最新用户信息，不使用缓存
        User user = userMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        // 直接从数据库获取最新用户信息，不使用缓存
        User user = userMapper.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return user;
    }

    @Override
    public User updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = getUserById(userId);
        String oldUsername = user.getUsername();

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userMapper.existsByUsername(request.getUsername())) {
                throw new BusinessException(400, "用户名已被占用");
            }
            user.setUsername(request.getUsername());
        }
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (!request.getPhone().isEmpty() && userMapper.existsByPhone(request.getPhone())) {
                throw new BusinessException(400, "手机号已被占用");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (!request.getEmail().isEmpty() && userMapper.existsByEmail(request.getEmail())) {
                throw new BusinessException(400, "邮箱已被占用");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getIdCard() != null) user.setIdCard(request.getIdCard());
        
        User updatedUser = userMapper.save(user);
        
        // 清除缓存
        userCacheService.evictUser(userId);
        if (!oldUsername.equals(updatedUser.getUsername())) {
            userCacheService.evictUserByUsername(oldUsername);
        }
        userCacheService.evictUserByUsername(updatedUser.getUsername());
        
        return updatedUser;
    }

    @Override
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.save(user);
        
        // 清除缓存
        userCacheService.evictUser(userId);
        userCacheService.evictUserByUsername(user.getUsername());
    }

    @Override
    public String updateAvatar(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的图片");
        }
        if (!FileUtil.isImage(file)) {
            throw new BusinessException(400, "只支持上传图片文件");
        }
        try {
            String avatarUrl = FileUtil.saveFile(file, uploadDir);
            User user = getUserById(userId);
            user.setAvatar(avatarUrl);
            userMapper.save(user);
            
            // 清除缓存
            userCacheService.evictUser(userId);
            userCacheService.evictUserByUsername(user.getUsername());
            
            return avatarUrl;
        } catch (Exception e) {
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public BigDecimal getBalance(Long userId) {
        // 直接从数据库获取最新余额，不使用缓存
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return user.getBalance();
    }

    @Override
    public void updateRole(Long userId, Integer role) {
        User user = getUserById(userId);
        user.setRole(role);
        userMapper.save(user);
        
        // 清除缓存
        userCacheService.evictUser(userId);
        userCacheService.evictUserByUsername(user.getUsername());
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        User user = getUserById(userId);
        user.setStatus(status);
        userMapper.save(user);
        
        // 清除缓存
        userCacheService.evictUser(userId);
        userCacheService.evictUserByUsername(user.getUsername());
    }

    @Override
    public Page<User> getAllUsers(String keyword, Integer role, Integer status, Pageable pageable) {
        return userMapper.findAll((root, query, cb) -> {
            var predicates = cb.conjunction();
            
            if (keyword != null && !keyword.isEmpty()) {
                predicates = cb.and(predicates, cb.or(
                    cb.like(root.get("username"), "%" + keyword + "%"),
                    cb.like(root.get("phone"), "%" + keyword + "%"),
                    cb.like(root.get("email"), "%" + keyword + "%")
                ));
            }
            
            if (role != null) {
                predicates = cb.and(predicates, cb.equal(root.get("role"), role));
            }
            
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }
            
            return predicates;
        }, pageable);
    }

    @Override
    public long countUsers() {
        return userMapper.count();
    }

    @Override
    public long countProviders() {
        return userMapper.countByRole(Constants.ROLE_PROVIDER);
    }

    @Override
    public User adminUpdateUser(Long userId, UserUpdateRequest request) {
        User user = getUserById(userId);
        String oldUsername = user.getUsername();
        
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userMapper.existsByUsername(request.getUsername())) {
                throw new BusinessException(400, "用户名已存在");
            }
            user.setUsername(request.getUsername());
        }
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (request.getPhone() != null && !request.getPhone().isEmpty() && userMapper.existsByPhone(request.getPhone())) {
                throw new BusinessException(400, "手机号已被注册");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (request.getEmail() != null && !request.getEmail().isEmpty() && userMapper.existsByEmail(request.getEmail())) {
                throw new BusinessException(400, "邮箱已被注册");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getIdCard() != null) user.setIdCard(request.getIdCard());
        
        User updatedUser = userMapper.save(user);
        
        // 清除缓存
        userCacheService.evictUser(userId);
        if (!oldUsername.equals(updatedUser.getUsername())) {
            userCacheService.evictUserByUsername(oldUsername);
        }
        userCacheService.evictUserByUsername(updatedUser.getUsername());
        
        return updatedUser;
    }

    @Override
    public void adminResetPassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BusinessException(400, "新密码不能为空");
        }
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.save(user);
        
        // 清除缓存
        userCacheService.evictUser(userId);
        userCacheService.evictUserByUsername(user.getUsername());
    }

    @Override
    public void adminAdjustBalance(Long userId, BigDecimal amount, String remark) {
        User user = getUserById(userId);
        BigDecimal newBalance = user.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(400, "调整后余额不能为负数");
        }
        user.setBalance(newBalance);
        userMapper.save(user);
        
        // 清除缓存
        userCacheService.evictUser(userId);
        userCacheService.evictUserByUsername(user.getUsername());
        userCacheService.evictUserBalance(userId);
    }

    @Override
    public void resetPassword(String phone, String code, String newPassword) {
        String redisKey = "sms:code:" + phone;
        String storedCode = redisUtil.get(redisKey);

        if (storedCode == null) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }

        if (!storedCode.equals(code)) {
            throw new BusinessException(400, "验证码错误");
        }

        User user = userMapper.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(404, "该手机号未注册"));

        if (user.getStatus() == Constants.STATUS_DISABLED) {
            throw new BusinessException(403, "账号已被禁用");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.save(user);

        redisUtil.delete(redisKey);

        userCacheService.evictUser(user.getId());
        userCacheService.evictUserByUsername(user.getUsername());
    }
}
