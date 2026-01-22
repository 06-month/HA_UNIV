package com.university.grade.repository;

import com.university.grade.entity.GradeSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeSummaryRepository extends JpaRepository<GradeSummary, Long> {
    Optional<GradeSummary> findByStudentStudentIdAndSemester(Long studentId, String semester);
    
    @Query("SELECT gs FROM GradeSummary gs " +
           "WHERE gs.student.studentId = :studentId")
    List<GradeSummary> findByStudentStudentId(@Param("studentId") Long studentId);
}
