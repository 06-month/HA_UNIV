package com.university.grade.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "GRADE_SUMMARY",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "semester"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "semester", nullable = false, length = 20)
    private String semester;

    @Column(name = "gpa", nullable = false, precision = 4, scale = 2)
    private BigDecimal gpa;

    @Column(name = "total_credits", nullable = false)
    private Integer totalCredits;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
