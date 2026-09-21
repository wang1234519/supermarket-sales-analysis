package com.supermarket.dto;

import lombok.Data;

/**
 * 用户信息（返回给前端，不含密码）
 */
@Data
public class UserInfo {

    private Long id;

    private String username;

    private String nickname;

    private String role;
}
