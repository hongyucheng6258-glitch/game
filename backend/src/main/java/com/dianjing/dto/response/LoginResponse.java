package com.dianjing.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private UserInfo user;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String phone;
        private String email;
        private String avatar;
        private Integer role;
    }
}
