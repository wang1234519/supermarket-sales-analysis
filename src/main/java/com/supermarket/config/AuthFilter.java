package com.supermarket.config;

import com.supermarket.service.AuthService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 页面访问过滤器：未登录访问业务页面时跳转到登录页。
 * 静态资源（css/js）和 /api/** 放行（接口由 AuthInterceptor 统一返回 401）。
 */
@Component
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        String uri = request.getRequestURI();

        // 放行：登录/注册页、静态资源、认证接口
        if (uri.equals("/login.html") || uri.equals("/register.html")
                || uri.startsWith("/css/") || uri.startsWith("/js/")
                || uri.equals("/favicon.ico") || uri.startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        // 已登录：放行
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthService.SESSION_USER) != null) {
            chain.doFilter(request, response);
            return;
        }

        // API 交给拦截器返回 401 JSON，页面直接重定向到登录页
        if (uri.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }
        response.sendRedirect("/login.html");
    }
}
