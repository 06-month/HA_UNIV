package com.university.grade.service;

import com.university.grade.dto.LoginRequest;
import com.university.grade.dto.LoginResponse;
import com.university.grade.entity.Student;
import com.university.grade.entity.User;
import com.university.grade.repository.StudentRepository;
import com.university.grade.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // loginId로 사용자 찾기 (학번 또는 사번)
        Optional<User> userOpt = userRepository.findByLoginId(request.getUserId());
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("학번/사번 또는 비밀번호가 올바르지 않습니다.");
        }

        User user = userOpt.get();
        
        // 비밀번호 검증
        log.debug("Login attempt - userId: {}, storedHash: {}", request.getUserId(), user.getPasswordHash());
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        log.debug("Password match result: {}", passwordMatches);
        
        if (!passwordMatches) {
            log.warn("Login failed - userId: {}, password mismatch", request.getUserId());
            throw new RuntimeException("학번/사번 또는 비밀번호가 올바르지 않습니다.");
        }

        // 학생 정보 조회
        Optional<Student> studentOpt = studentRepository.findByUserUserId(user.getUserId());
        Long studentId = studentOpt.map(Student::getStudentId).orElse(null);
        String name = studentOpt.map(Student::getName).orElse("사용자"); // 학생 이름, 없으면 기본값

        return LoginResponse.builder()
                .userId(user.getUserId())
                .studentId(studentId)
                .role(user.getRole())
                .message("로그인 성공")
                .name(name)
                .build();
    }
}
