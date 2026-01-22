package com.university.grade.controller;

import com.university.grade.dto.LoginRequest;
import com.university.grade.dto.LoginResponse;
import com.university.grade.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            LoginResponse response = authService.login(request);
            
            log.info("Login successful - userId: {}, studentId: {}, role: {}", 
                    response.getUserId(), response.getStudentId(), response.getRole());
            
            // 세션에 사용자 정보 저장
            session.setAttribute("userId", response.getUserId());
            session.setAttribute("studentId", response.getStudentId());
            session.setAttribute("role", response.getRole());
            
            log.debug("Session attributes set - userId: {}, studentId: {}, role: {}", 
                    session.getAttribute("userId"), 
                    session.getAttribute("studentId"), 
                    session.getAttribute("role"));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }
}
