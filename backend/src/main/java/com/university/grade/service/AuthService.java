package com.university.grade.service;

import com.university.grade.dto.LoginRequest;
import com.university.grade.dto.LoginResponse;
import com.university.grade.entity.Student;
import com.university.grade.entity.User;
import com.university.grade.repository.StudentRepository;
import com.university.grade.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
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
        Optional<User> userOpt;
        
        try {
            // loginId로 사용자 찾기 (학번 또는 사번)
            log.debug("Attempting to find user with loginId: {}", request.getUserId());
            userOpt = userRepository.findByLoginId(request.getUserId());
            
            if (userOpt.isEmpty()) {
                log.warn("User not found - loginId: {}", request.getUserId());
                throw new RuntimeException("학번/사번 또는 비밀번호가 올바르지 않습니다.");
            }
        } catch (DataAccessException e) {
            // 데이터베이스 연결 오류를 명확히 구분
            log.error("Database access error during login - loginId: {}, error: {}, cause: {}", 
                    request.getUserId(), e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "N/A", e);
            throw new RuntimeException("데이터베이스 연결에 실패했습니다. 서버 관리자에게 문의하세요.", e);
        } catch (RuntimeException e) {
            // 이미 처리된 RuntimeException은 그대로 전달
            throw e;
        } catch (Exception e) {
            // 기타 예외도 로깅
            log.error("Unexpected error during user lookup - loginId: {}, error: {}", 
                    request.getUserId(), e.getMessage(), e);
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다.", e);
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
