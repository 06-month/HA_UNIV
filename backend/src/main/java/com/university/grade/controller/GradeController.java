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

        try {
            List<String> semesters = gradeInquiryService.getAvailableSemesters(studentId);
            return ResponseEntity.ok(semesters);
        } catch (Exception e) {
            log.error("Failed to get available semesters: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
