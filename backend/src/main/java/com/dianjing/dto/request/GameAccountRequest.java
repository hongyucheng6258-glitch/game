package com.dianjing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GameAccountRequest {
    @NotBlank(message = "游戏类型不能为空")
    private String gameType;
    @NotBlank(message = "游戏账号名称不能为空")
    private String accountName;
    private String accountPassword;
    private String accountLevel;
    private String region;
    private String accountDescription;
}
