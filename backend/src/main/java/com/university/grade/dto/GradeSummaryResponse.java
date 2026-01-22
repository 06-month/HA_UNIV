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
public class GradeSummaryResponse {
    private String semester;
    private BigDecimal gpa;
    private Integer totalCredits;
}
