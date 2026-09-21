package com.supermarket.service;

import com.supermarket.dto.LoginRequest;
import com.supermarket.dto.PasswordRequest;
import com.supermarket.dto.RegisterRequest;
import com.supermarket.dto.UserInfo;
import com.supermarket.entity.User;
import com.supermarket.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 登录认证服务（会话方式，密码 BCrypt 加密）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    /** 会话中保存登录用户的属性名 */
    public static final String SESSION_USER = "loginUser";

    /** 会话有效期：30 分钟 */
    private static final int NORMAL_SECONDS = 30 * 60;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /** 登录 */
    public Map<String, Object> login(LoginRequest req, HttpSession session) {
        if (!StringUtils.hasText(req.getUsername()) || !StringUtils.hasText(req.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        User user = userMapper.selectByUsername(req.getUsername().trim());
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        UserInfo info = toInfo(user);
        session.setAttribute(SESSION_USER, info);
        session.setMaxInactiveInterval(NORMAL_SECONDS);
        log.info("用户登录成功：{}", user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("user", info);
        return result;
    }

    /** 注册（注册成功后自动登录） */
    public Map<String, Object> register(RegisterRequest req, HttpSession session) {
        if (!StringUtils.hasText(req.getUsername()) || !StringUtils.hasText(req.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        if (!USERNAME_PATTERN.matcher(req.getUsername().trim()).matches()) {
            throw new IllegalArgumentException("用户名需为 3~20 位字母、数字或下划线");
        }
        if (req.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码长度至少 6 位");
        }
        if (userMapper.selectByUsername(req.getUsername().trim()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(req.getUsername().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(StringUtils.hasText(req.getNickname()) ? req.getNickname().trim() : req.getUsername().trim());
        user.setRole("user");
        userMapper.insert(user);
        log.info("新用户注册：{}", user.getUsername());

        // 注册成功后直接登录
        LoginRequest login = new LoginRequest();
        login.setUsername(user.getUsername());
        login.setPassword(req.getPassword());
        return login(login, session);
    }

    /** 修改密码 */
    public Map<String, Object> changePassword(Long userId, PasswordRequest req) {
        if (!StringUtils.hasText(req.getOldPassword()) || !StringUtils.hasText(req.getNewPassword())) {
            throw new IllegalArgumentException("原密码和新密码不能为空");
        }
        if (req.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("新密码长度至少 6 位");
        }
        User current = userMapper.selectById(userId);
        if (current == null || !passwordEncoder.matches(req.getOldPassword(), current.getPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(req.getNewPassword()));
        log.info("用户 {} 修改了密码", current.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "密码修改成功");
        return result;
    }

    /** 当前登录用户 */
    public UserInfo me(HttpSession session) {
        return (UserInfo) session.getAttribute(SESSION_USER);
    }

    /** 退出登录 */
    public void logout(HttpSession session) {
        session.invalidate();
    }

    /** 会话中的用户信息转 UserInfo */
    public UserInfo toInfo(User user) {
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setNickname(user.getNickname());
        info.setRole(user.getRole());
        return info;
    }
}
