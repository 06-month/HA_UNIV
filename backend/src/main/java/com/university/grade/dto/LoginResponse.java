package com.university.grade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Long userId;
    private Long studentId;
    private String role;
    private String message;
    private String name; // 사용자 이름 (학생 이름 또는 교직원 이름)
}
