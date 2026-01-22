package com.university.grade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeDetailResponse {
    private Long enrollmentId;
    private String courseCode;
    private String courseName;
    private Integer credit;
    private String gradeLetter;
    private BigDecimal score;
    private Boolean isFinalized;
}
