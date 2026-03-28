package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.vo.EnrollBriefVO;
import com.bluenet.web.domain.model.vo.EnrollStatisticsVO;
import com.bluenet.web.domain.model.vo.EnrollVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EnrollRepository {
    Optional<EnrollVO> findById(Long id);
    Optional<EnrollVO> findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);
    Long save(EnrollVO enroll);
    void update(EnrollVO enroll);
    Page<EnrollBriefVO> findAll(Pageable pageable);
    Page<EnrollBriefVO> findByStatus(EnrollStatus status, Pageable pageable);
    Page<EnrollBriefVO> findByDirection(Direction direction, Pageable pageable);
    Page<EnrollBriefVO> findByStatusAndDirection(EnrollStatus status, Direction direction, Pageable pageable);
    Page<EnrollBriefVO> search(String keyword, EnrollStatus status, Direction direction, Pageable pageable);
    EnrollStatisticsVO getStatistics();
}
