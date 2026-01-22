package com.university.grade.controller;

import com.university.grade.dto.ObjectionRequest;
import com.university.grade.dto.ObjectionResponse;
import com.university.grade.service.ObjectionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/objections")
@RequiredArgsConstructor
@Slf4j
public class ObjectionController {
    private final ObjectionService objectionService;

    @PostMapping
    public ResponseEntity<ObjectionResponse> createObjection(
            @RequestBody ObjectionRequest request,
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
            ObjectionResponse response = objectionService.createObjection(studentId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to create objection: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ObjectionResponse.builder()
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Failed to create objection: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
