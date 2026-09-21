package com.supermarket.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户表实体（sys_user）
 */
@Data
public class User {

    /** 用户ID */
    private Long id;

    /** 登录账号 */
    private String username;

    /** 密码（BCrypt 加密存储） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 角色：admin / user */
    private String role;

    /** 创建时间 */
    private LocalDateTime createTime;
}
