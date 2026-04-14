package com.dianjing.controller;

import com.dianjing.common.Result;
import com.dianjing.dto.request.GameAccountRequest;
import com.dianjing.entity.GameAccount;
import com.dianjing.service.GameAccountService;
import com.dianjing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/game-accounts")
public class GameAccountController {

    private final GameAccountService gameAccountService;
    private final UserService userService;

    public GameAccountController(GameAccountService gameAccountService, UserService userService) {
        this.gameAccountService = gameAccountService;
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username).getId();
    }

    @PostMapping
    public Result<GameAccount> create(@Valid @RequestBody GameAccountRequest request) {
        Long userId = getCurrentUserId();
        GameAccount created = gameAccountService.create(userId, request);
        return Result.success(created);
    }

    @GetMapping
    public Result<List<GameAccount>> list(@RequestParam(required = false) String gameType) {
        Long userId = getCurrentUserId();
        return Result.success(gameAccountService.getByUserId(userId));
    }

    @GetMapping("/{id}")
    public Result<GameAccount> getById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        GameAccount account = gameAccountService.getById(userId, id);
        return Result.success(account);
    }

    @PutMapping("/{id}")
    public Result<GameAccount> update(@PathVariable Long id, @Valid @RequestBody GameAccountRequest request) {
        Long userId = getCurrentUserId();
        GameAccount updated = gameAccountService.update(userId, id, request);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        gameAccountService.delete(userId, id);
        return Result.success();
    }
}
