package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.assessment_time.AssessmentProgressDTO;
import com.bluenet.web.api.dto.assessment_time.AssessmentTimeDTO;
import com.bluenet.web.api.dto.assessment_time.CreateAssessmentTimeRequestDTO;
import com.bluenet.web.api.dto.assessment_time.UpdateAssessmentTimeRequestDTO;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.application.converter.AssessmentTimeConverter;
import com.bluenet.web.application.service.AssessmentTimeService;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.service.AssessmentTimeDomainService;
import com.bluenet.web.domain.util.GradeCalculator;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.infrastructure.security.util.RoleHierarchy;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 考核时间应用服务实现类
 * <p>
 * 协调领域服务完成考核时间的业务操作，实现基于角色的查询过滤
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AssessmentTimeServiceImpl implements AssessmentTimeService {
    private final AssessmentTimeDomainService assessmentTimeDomainService;
    private final AssessmentTimeConverter assessmentTimeConverter;
    private final AssessmentTimeRepository assessmentTimeRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;

    @Override
    @Transactional
    public AssessmentTimeDTO createAssessmentTime(CreateAssessmentTimeRequestDTO request) {
        AssessmentTimeVO vo = AssessmentTimeVO.builder()
                .direction(request.getDirection())
                .epoch(request.getEpoch())
                .grade(request.getGrade())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .timeLimit(request.getTimeLimit())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .build();

        Long id = assessmentTimeDomainService.create(vo);

        Optional<AssessmentTimeVO> created = assessmentTimeDomainService.getById(id);
        if (created.isEmpty()) {
            throw new GlobalException("创建考核时间失败");
        }

        return assessmentTimeConverter.convertToDTO(created.get());
    }

    @Override
    @Transactional
    public AssessmentTimeDTO updateAssessmentTime(Long id, UpdateAssessmentTimeRequestDTO request) {
        AssessmentTimeVO vo = AssessmentTimeVO.builder()
                .id(id)
                .direction(request.getDirection())
                .epoch(request.getEpoch())
                .grade(request.getGrade())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .timeLimit(request.getTimeLimit())
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .build();

        assessmentTimeDomainService.update(vo);

        Optional<AssessmentTimeVO> updated = assessmentTimeDomainService.getById(id);
        if (updated.isEmpty()) {
            throw new GlobalException("更新考核时间失败");
        }

        return assessmentTimeConverter.convertToDTO(updated.get());
    }

    @Override
    @Transactional
    public void deleteAssessmentTime(Long id) {
        assessmentTimeDomainService.delete(id);
    }

    @Override
    public PageDTO<AssessmentTimeDTO> listAssessmentTimes(Integer page, Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 5;

        UserVO currentUser = UserCTX.getCurrentUser();
        Direction direction = null;
        Integer grade = null;

        if (currentUser != null) {
            RoleType roleType = RoleType.fromName(currentUser.getRoleName());
            if (roleType != null && !RoleHierarchy.isDirectionAdminOrAbove(roleType)) {
                // MEMBER 或 CANDIDATE：按方向过滤
                direction = currentUser.getDirection();

                if (roleType == RoleType.CANDIDATE) {
                    // CANDIDATE：还需要按年级过滤
                    grade = GradeCalculator.calculateGrade(currentUser.getStudentId());
                }
            }
        }

        Page<AssessmentTimeVO> voPage = assessmentTimeRepository.findByFilters(
                direction,
                grade,
                PageRequest.of(pageNum, pageSize));
        Page<AssessmentTimeDTO> dtoPage = voPage.map(assessmentTimeConverter::convertToDTO);
        return PageDTO.from(dtoPage);
    }

    @Override
    public PageDTO<AssessmentTimeDTO> listAssessmentTimesForUser(Integer page, Integer size) {
        PageDTO<AssessmentTimeDTO> result = listAssessmentTimes(page, size);

        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser != null) {
            for (AssessmentTimeDTO dto : result.getContent()) {
                int totalQuestions = assessmentQuestionRepository.countByAssessmentTimeId(dto.getId());
                int completedQuestions = assessmentAnswerRepository
                        .countByUserIdAndAssessmentTimeId(currentUser.getId(), dto.getId());
                dto.setTotalQuestions(totalQuestions);
                dto.setCompletedQuestions(completedQuestions);
            }
        }

        return result;
    }

    @Override
    public AssessmentProgressDTO getAssessmentProgress(Long assessmentTimeId) {
        assessmentTimeDomainService.getById(assessmentTimeId)
                .orElseThrow(() -> new IllegalArgumentException("考核时间不存在"));

        UserVO currentUser = UserCTX.getCurrentUser();
        int totalQuestions = assessmentQuestionRepository.countByAssessmentTimeId(assessmentTimeId);
        int completedQuestions = 0;
        if (currentUser != null) {
            completedQuestions = assessmentAnswerRepository
                    .countByUserIdAndAssessmentTimeId(currentUser.getId(), assessmentTimeId);
        }

        return AssessmentProgressDTO.builder()
                .assessmentTimeId(assessmentTimeId)
                .totalQuestions(totalQuestions)
                .completedQuestions(completedQuestions)
                .build();
    }
}
