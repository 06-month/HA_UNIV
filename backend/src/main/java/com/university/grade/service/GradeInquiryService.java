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

    @Cacheable(value = "availableSemesters", key = "'semesters:' + #studentId", condition = "#studentId != null", unless = "#result == null || #result.isEmpty()")
    @Transactional(readOnly = true)
    public List<String> getAvailableSemesters(Long studentId) {
        log.info("getAvailableSemesters called with studentId: {}", studentId);
        try {
            // 1. GradeSummary에서 학기 목록 가져오기 (가장 확실한 데이터)
            List<GradeSummary> summaries = summaryRepository.findByStudentStudentId(studentId);
            log.info("Found {} grade summaries for studentId {}", summaries.size(), studentId);
            
            List<String> semestersFromSummary = summaries.stream()
                    .map(GradeSummary::getSemester)
                    .filter(semester -> semester != null && !semester.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            
            // 2. Enrollment에서도 학기 목록 가져오기 (GradeSummary에 없는 경우 대비)
            List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
            log.debug("Found {} enrollments for studentId {}", enrollments.size(), studentId);
            
            List<String> semestersFromEnrollment = enrollments.stream()
                    .map(Enrollment::getSemester)
                    .filter(semester -> semester != null && !semester.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            
            // 3. 두 리스트를 합치고 중복 제거
            java.util.Set<String> semesterSet = new java.util.HashSet<>();
            semesterSet.addAll(semestersFromSummary);
            semesterSet.addAll(semestersFromEnrollment);
            
            List<String> semesters = semesterSet.stream()
                    .sorted((a, b) -> b.compareTo(a)) // 최신 학기부터
                    .collect(Collectors.toList());
            
            log.info("Available semesters for studentId {}: {} (from {} summaries, {} enrollments)", 
                    studentId, semesters, summaries.size(), enrollments.size());
            
            if (semesters.isEmpty()) {
                log.warn("No semesters found for studentId {} from any source", studentId);
            }
            
            return semesters;
        } catch (Exception e) {
            log.error("Error getting available semesters for studentId {}: {}", studentId, e.getMessage(), e);
            return List.of();
        }
    }
}
