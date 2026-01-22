package com.university.grade.repository;

import com.university.grade.entity.Grade;
import com.university.grade.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    Optional<Grade> findByEnrollment(Enrollment enrollment);
    
    @Query("SELECT g FROM Grade g " +
           "JOIN g.enrollment e " +
           "JOIN e.student s " +
           "WHERE s.studentId = :studentId AND e.semester = :semester")
    List<Grade> findByStudentIdAndSemester(@Param("studentId") Long studentId, @Param("semester") String semester);
}
