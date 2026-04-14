package com.dianjing.controller;

import com.dianjing.common.Result;
import com.dianjing.dto.request.LoginRequest;
import com.dianjing.dto.request.PhoneLoginRequest;
import com.dianjing.dto.request.RegisterRequest;
import com.dianjing.dto.request.ResetPasswordRequest;
import com.dianjing.dto.request.SendSmsRequest;
import com.dianjing.dto.response.LoginResponse;
import com.dianjing.entity.User;
import com.dianjing.security.JwtTokenProvider;
import com.dianjing.service.TokenCacheService;
import com.dianjing.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class AuthController {

    private final UserService userService;
    private final TokenCacheService tokenCacheService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService,
                         TokenCacheService tokenCacheService,
                         JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.tokenCacheService = tokenCacheService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request);
        User user = userService.getUserByUsername(request.getUsername());

        LoginResponse response = new LoginResponse();
        response.setToken(token);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole(user.getRole());
        response.setUser(userInfo);

        return Result.success(response);
    }

    @PostMapping("/send-sms")
    public Result<String> sendSms(@Valid @RequestBody SendSmsRequest request) {
        String code = userService.sendSmsCode(request.getPhone());
        return Result.success(code);
    }

    @PostMapping("/phone-login")
    public Result<LoginResponse> phoneLogin(@Valid @RequestBody PhoneLoginRequest request) {
        String token = userService.phoneLogin(request);
        User user = userService.getUserByPhone(request.getPhone());

        LoginResponse response = new LoginResponse();
        response.setToken(token);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole(user.getRole());
        response.setUser(userInfo);

        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            long expiration = jwtTokenProvider.getExpiration();
            tokenCacheService.addToBlacklist(token, expiration);
        }
        return Result.success();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getPhone(), request.getCode(), request.getNewPassword());
        return Result.success();
    }

    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        if (!StringUtils.hasText(username)) {
            return Result.success(false);
        }
        boolean exists = userService.existsByUsername(username);
        return Result.success(exists);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
