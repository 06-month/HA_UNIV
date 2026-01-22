package com.university.grade.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectionRequest {
    private Long enrollmentId;
    private String title;
    private String reason;
}
