package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.vo.EnrollBriefVO;
import com.bluenet.web.domain.model.vo.EnrollStatisticsVO;
import com.bluenet.web.domain.model.vo.EnrollVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EnrollDomainService {
    Optional<EnrollVO> getEnrollmentById(Long id);
    Optional<EnrollVO> getEnrollmentByStudentId(String studentId);
    boolean existsByStudentId(String studentId);
    Long createEnrollment(EnrollVO enrollment);
    void updateEnrollment(EnrollVO enrollment);
    Page<EnrollBriefVO> getEnrollmentList(String keyword, EnrollStatus status, Direction direction, Pageable pageable);
    EnrollStatisticsVO getStatistics();
    void approveEnrollment(Long id);
    void approveEnrollment(Long id, Integer assessmentGradeYear);
    void rejectEnrollment(Long id, String reason);
    void validateAvatar(Long avatarId);
}
