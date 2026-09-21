package com.supermarket.dto;

import lombok.Data;

/**
 * 修改密码请求
 */
@Data
public class PasswordRequest {

    private String oldPassword;

    private String newPassword;
}
