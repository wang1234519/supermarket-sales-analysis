package com.supermarket.controller;

import com.supermarket.dto.LoginRequest;
import com.supermarket.dto.PasswordRequest;
import com.supermarket.dto.RegisterRequest;
import com.supermarket.dto.UserInfo;
import com.supermarket.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录认证接口（本控制器的接口不需要登录）
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 登录 */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req, HttpSession session) {
        return authService.login(req, session);
    }

    /** 注册（成功后自动登录） */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest req, HttpSession session) {
        return authService.register(req, session);
    }

    /** 当前登录用户；未登录返回 401（由拦截器处理） */
    @GetMapping("/me")
    public UserInfo me(HttpSession session) {
        return authService.me(session);
    }

    /** 修改密码（需登录，拦截器已保证，此处再兜底校验） */
    @PostMapping("/password")
    public Map<String, Object> password(@RequestBody PasswordRequest req, HttpSession session) {
        UserInfo current = authService.me(session);
        if (current == null) {
            throw new IllegalArgumentException("未登录或登录已过期，请重新登录");
        }
        return authService.changePassword(current.getId(), req);
    }

    /** 退出登录 */
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        authService.logout(session);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已退出登录");
        return result;
    }
}
