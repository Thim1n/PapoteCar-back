package com.PapoteCar.PapoteCar.interceptor;

import com.PapoteCar.PapoteCar.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.getWriter().write("Token invalide");
            return false;
        }

        String token = authHeader.substring(7);

        if(!jwtUtil.isTokenValid(token)) {
            response.setStatus(401);
            response.getWriter().write("Token invalide");
            return false;
        }

        return true;
    }
}
