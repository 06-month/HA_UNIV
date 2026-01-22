package com.university.grade.repository;

import com.university.grade.entity.GradeObjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeObjectionRepository extends JpaRepository<GradeObjection, Long> {
}
