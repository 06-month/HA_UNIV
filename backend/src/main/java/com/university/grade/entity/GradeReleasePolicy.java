package com.university.grade.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "GRADE_RELEASE_POLICY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeReleasePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "semester", unique = true, nullable = false, length = 20)
    private String semester;

    @Column(name = "is_released", nullable = false)
    private Boolean isReleased = false;

    @Column(name = "release_at")
    private LocalDateTime releaseAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
