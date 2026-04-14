package com.dianjing.dto.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String username;
    private String avatar;
    private String phone;
    private String email;
    private String realName;
    private String idCard;
}
