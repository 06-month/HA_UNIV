package com.university.grade.repository;

import com.university.grade.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    @Query("SELECT e FROM Enrollment e " +
           "JOIN e.student s " +
           "WHERE s.studentId = :studentId AND e.semester = :semester")
    List<Enrollment> findByStudentIdAndSemester(@Param("studentId") Long studentId, @Param("semester") String semester);
    
    @Query("SELECT e FROM Enrollment e " +
           "JOIN e.student s " +
           "WHERE s.studentId = :studentId")
    List<Enrollment> findByStudentId(@Param("studentId") Long studentId);
    
    Optional<Enrollment> findByEnrollmentId(Long enrollmentId);
}
