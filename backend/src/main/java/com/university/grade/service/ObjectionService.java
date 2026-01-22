package com.university.grade.service;

import com.university.grade.dto.ObjectionRequest;
import com.university.grade.dto.ObjectionResponse;
import com.university.grade.entity.*;
import com.university.grade.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObjectionService {
    private final EnrollmentRepository enrollmentRepository;
    private final GradeObjectionRepository objectionRepository;
    private final SystemEventRepository eventRepository;

    @Transactional
    @CacheEvict(value = {"gradeList", "gradeSummary"}, allEntries = true)
    public ObjectionResponse createObjection(Long studentId, ObjectionRequest request) {
        // 수강 신청 정보 조회 및 검증
        Enrollment enrollment = enrollmentRepository.findByEnrollmentId(request.getEnrollmentId())
                .orElseThrow(() -> new RuntimeException("수강 신청 정보를 찾을 수 없습니다."));

        // 학생 본인의 수강 신청인지 확인
        if (!enrollment.getStudent().getStudentId().equals(studentId)) {
            throw new RuntimeException("본인의 성적에 대해서만 이의신청할 수 있습니다.");
        }

        // 이의신청 저장
        GradeObjection objection = GradeObjection.builder()
                .enrollment(enrollment)
                .title(request.getTitle())
                .reason(request.getReason())
                .status(GradeObjection.ObjectionStatus.PENDING)
                .build();

        GradeObjection saved = objectionRepository.save(objection);

        // 시스템 이벤트 발행 (캐시 무효화용)
        SystemEvent event = SystemEvent.builder()
                .eventType("CACHE_INVALIDATION")
                .description("Grade objection created: " + saved.getObjectionId())
                .processingStatus(SystemEvent.ProcessingStatus.PENDING)
                .build();
        eventRepository.save(event);

        return ObjectionResponse.builder()
                .objectionId(saved.getObjectionId())
                .message("이의신청이 접수되었습니다.")
                .build();
    }
}
