package com.university.grade.repository;

import com.university.grade.entity.GradeSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradeSummaryRepository extends JpaRepository<GradeSummary, Long> {
    Optional<GradeSummary> findByStudentStudentIdAndSemester(Long studentId, String semester);
}
