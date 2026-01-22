package com.university.grade.controller;

import com.university.grade.dto.GradeDetailResponse;
import com.university.grade.dto.GradeSummaryResponse;
import com.university.grade.service.GradeInquiryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
@Slf4j
public class GradeController {
    private final GradeInquiryService gradeInquiryService;

    @GetMapping("/semesters")
    public ResponseEntity<List<String>> getAvailableSemesters(
            @RequestHeader(value = "X-Student-Id", required = false) String studentIdHeader,
            HttpSession session) {
        
        log.info("getAvailableSemesters called - sessionId: {}, X-Student-Id header: {}", 
                session.getId(), studentIdHeader);
        
        // 세션에서 studentId 가져오기 (우선순위 1)
        Long studentId = (Long) session.getAttribute("studentId");
        log.info("studentId from session: {}", studentId);
        
        // 세션에 없으면 헤더에서 가져오기 (우선순위 2)
        if (studentId == null && studentIdHeader != null) {
            try {
                studentId = Long.parseLong(studentIdHeader);
                log.info("studentId from header (parsed): {}", studentId);
            } catch (NumberFormatException e) {
                log.warn("Invalid studentId header format: {}", studentIdHeader);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        
        if (studentId == null) {
            log.error("studentId is null - session attributes: userId={}, studentId={}, role={}, allAttributes={}", 
                    session.getAttribute("userId"), 
                    session.getAttribute("studentId"), 
                    session.getAttribute("role"),
                    java.util.Collections.list(session.getAttributeNames()));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.info("Using studentId: {} for semester query", studentId);

        try {
            List<String> semesters = gradeInquiryService.getAvailableSemesters(studentId);
            log.info("Available semesters for studentId {}: {}", studentId, semesters);
            return ResponseEntity.ok(semesters != null ? semesters : List.of());
        } catch (Exception e) {
            log.error("Failed to get available semesters for studentId {}: {}", studentId, e.getMessage(), e);
            // 에러가 발생해도 빈 리스트를 반환하여 프론트엔드에서 처리할 수 있도록 함
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<GradeSummaryResponse> getGradeSummary(
            @RequestParam String semester,
            @RequestHeader(value = "X-Student-Id", required = false) String studentIdHeader,
            HttpSession session) {
        
        // 세션에서 studentId 가져오기
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null && studentIdHeader != null) {
            try {
                studentId = Long.parseLong(studentIdHeader);
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        
        if (studentId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 성적 공개 기간 확인
        if (!gradeInquiryService.isGradeReleased(semester)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            GradeSummaryResponse response = gradeInquiryService.getGradeSummary(studentId, semester);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get grade summary: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<GradeDetailResponse>> getGradeList(
            @RequestParam String semester,
            @RequestHeader(value = "X-Student-Id", required = false) String studentIdHeader,
            HttpSession session) {
        
        // 세션에서 studentId 가져오기
        Long studentId = (Long) session.getAttribute("studentId");
        if (studentId == null && studentIdHeader != null) {
            try {
                studentId = Long.parseLong(studentIdHeader);
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        
        if (studentId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 성적 공개 기간 확인
        if (!gradeInquiryService.isGradeReleased(semester)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<GradeDetailResponse> response = gradeInquiryService.getGradeList(studentId, semester);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get grade list: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
