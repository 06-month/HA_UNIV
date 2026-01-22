package com.university.grade.service;

import com.university.grade.dto.GradeDetailResponse;
import com.university.grade.dto.GradeSummaryResponse;
import com.university.grade.entity.*;
import com.university.grade.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeInquiryService {
    private final GradeReleasePolicyRepository policyRepository;
    private final GradeSummaryRepository summaryRepository;
    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional(readOnly = true)
    public boolean isGradeReleased(String semester) {
        return policyRepository.findBySemester(semester)
                .map(GradeReleasePolicy::getIsReleased)
                .orElse(false);
    }

    @Cacheable(value = "gradeSummary", key = "#studentId + ':' + #semester")
    @Transactional(readOnly = true)
    public GradeSummaryResponse getGradeSummary(Long studentId, String semester) {
        return summaryRepository.findByStudentStudentIdAndSemester(studentId, semester)
                .map(summary -> GradeSummaryResponse.builder()
                        .semester(summary.getSemester())
                        .gpa(summary.getGpa())
                        .totalCredits(summary.getTotalCredits())
                        .build())
                .orElseThrow(() -> new RuntimeException("성적 요약을 찾을 수 없습니다."));
    }

    @Cacheable(value = "gradeList", key = "#studentId + ':' + #semester")
    @Transactional(readOnly = true)
    public List<GradeDetailResponse> getGradeList(Long studentId, String semester) {
        List<Grade> grades = gradeRepository.findByStudentIdAndSemester(studentId, semester);
        
        return grades.stream()
                .map(grade -> {
                    Enrollment enrollment = grade.getEnrollment();
                    Course course = enrollment.getCourse();
                    
                    return GradeDetailResponse.builder()
                            .enrollmentId(enrollment.getEnrollmentId())
                            .courseCode(course.getCourseCode())
                            .courseName(course.getCourseName())
                            .credit(course.getCredit())
                            .gradeLetter(grade.getGradeLetter())
                            .score(grade.getScore())
                            .isFinalized(grade.getIsFinalized())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Cacheable(value = "availableSemesters", key = "#studentId")
    @Transactional(readOnly = true)
    public List<String> getAvailableSemesters(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(Enrollment::getSemester)
                .distinct()
                .sorted((a, b) -> b.compareTo(a)) // 최신 학기부터
                .collect(Collectors.toList());
    }
}
