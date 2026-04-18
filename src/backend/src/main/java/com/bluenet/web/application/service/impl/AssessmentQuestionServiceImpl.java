package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.assessment_question.AssessmentQuestionDTO;
import com.bluenet.web.api.dto.assessment_question.CreateQuestionRequestDTO;
import com.bluenet.web.api.dto.assessment_question.UpdateQuestionRequestDTO;
import com.bluenet.web.api.dto.assessment_question.UserQuestionListResponse;
import com.bluenet.web.application.converter.AssessmentQuestionConverter;
import com.bluenet.web.application.service.AssessmentQuestionService;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.AssessmentSessionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.service.AssessmentQuestionDomainService;
import com.bluenet.web.domain.service.AssessmentSessionDomainService;
import com.bluenet.web.domain.service.AssessmentTimeDomainService;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.util.GradeCalculator;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.domain.model.vo.evaluation.QuestionContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 考题应用服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentQuestionServiceImpl implements AssessmentQuestionService {

    private final AssessmentQuestionDomainService assessmentQuestionDomainService;
    private final AssessmentTimeDomainService assessmentTimeDomainService;
    private final AssessmentSessionDomainService assessmentSessionDomainService;
    private final AssessmentQuestionConverter assessmentQuestionConverter;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final FileDomainService fileDomainService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AssessmentQuestionDTO createQuestion(CreateQuestionRequestDTO request) {
        // 校验考核时间是否存在
        AssessmentTimeVO timeVO = assessmentTimeDomainService.getById(request.getAssessmentTimeId())
                .orElseThrow(() -> new IllegalArgumentException("考核时间不存在"));

        QuestionContent questionContent = request.getContent() != null
                ? objectMapper.convertValue(request.getContent(), QuestionContent.class)
                : null;

        AssessmentQuestionVO vo = AssessmentQuestionVO.builder()
                .assessmentTimeId(request.getAssessmentTimeId())
                .questionNo(request.getQuestionNo())
                .questionType(request.getQuestionType())
                .title(request.getTitle())
                .content(questionContent)
                .attachmentId(request.getAttachmentId())
                .score(request.getScore())
                .build();

        AssessmentQuestionVO created = assessmentQuestionDomainService.createQuestion(vo);
        return assessmentQuestionConverter.convertToDTO(created);
    }

    @Override
    @Transactional
    public AssessmentQuestionDTO updateQuestion(Long id, UpdateQuestionRequestDTO request) {
        AssessmentQuestionVO existing = assessmentQuestionDomainService.getQuestionById(id);

        QuestionContent updatedContent = request.getContent() != null
                ? objectMapper.convertValue(request.getContent(), QuestionContent.class)
                : existing.getContent();

        AssessmentQuestionVO vo = AssessmentQuestionVO.builder()
                .id(id)
                .assessmentTimeId(existing.getAssessmentTimeId())
                .questionNo(request.getQuestionNo() != null ? request.getQuestionNo() : existing.getQuestionNo())
                .questionType(
                        request.getQuestionType() != null ? request.getQuestionType() : existing.getQuestionType())
                .title(request.getTitle() != null ? request.getTitle() : existing.getTitle())
                .content(updatedContent)
                .attachmentId(
                        request.getAttachmentId() != null ? request.getAttachmentId() : existing.getAttachmentId())
                .score(request.getScore() != null ? request.getScore() : existing.getScore())
                .build();

        AssessmentQuestionVO updated = assessmentQuestionDomainService.updateQuestion(vo);
        return assessmentQuestionConverter.convertToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        assessmentQuestionDomainService.deleteQuestion(id);
    }

    @Override
    public PageDTO<AssessmentQuestionDTO> listQuestionsForAdmin(Long assessmentTimeId, Integer page, Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        Page<AssessmentQuestionVO> voPage = assessmentQuestionDomainService.listQuestions(
                assessmentTimeId,
                PageRequest.of(pageNum, pageSize));
        Page<AssessmentQuestionDTO> dtoPage = voPage.map(assessmentQuestionConverter::convertToDTO);
        return PageDTO.from(dtoPage);
    }

    @Override
    public UserQuestionListResponse listQuestionsForUser(Long assessmentTimeId, Integer page, Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        // 校验考核时间是否存在
        AssessmentTimeVO timeVO = assessmentTimeDomainService.getById(assessmentTimeId)
                .orElseThrow(() -> new IllegalArgumentException("考核时间不存在"));

        // 权限校验
        UserVO currentUser = UserCTX.getCurrentUser();
        if (currentUser != null) {
            RoleType roleType = RoleType.fromName(currentUser.getRoleName());
            if (roleType == RoleType.CANDIDATE) {
                // 考生只能看自己方向+入学年份的考题
                if (!currentUser.getDirection().equals(timeVO.getDirection())) {
                    throw new SecurityException("无权查看该考核的题目");
                }
                Integer userEnrollmentYear = GradeCalculator.resolveAssessmentYear(
                        currentUser.getStudentId(),
                        currentUser.getAssessmentGradeYear());
                if (userEnrollmentYear != null && !userEnrollmentYear.equals(timeVO.getGrade())) {
                    throw new SecurityException("无权查看该考核的题目");
                }
            }
        }

        Page<AssessmentQuestionVO> voPage = assessmentQuestionDomainService.listQuestions(
                assessmentTimeId,
                PageRequest.of(pageNum, pageSize));

        // 转换为用户端DTO（不包含content），并填充答题状态
        Page<AssessmentQuestionDTO> dtoPage = voPage.map(vo -> {
            AssessmentQuestionDTO dto = assessmentQuestionConverter.convertToDTOForUser(vo);
            if (currentUser != null) {
                // 检查该用户是否已作答此题
                boolean answered = assessmentAnswerRepository
                        .existsByUserIdAndQuestionId(currentUser.getId(), vo.getId());
                dto.setAnswered(answered);
            } else {
                dto.setAnswered(false);
            }
            return dto;
        });

        // 限时考核：获取或创建会话，返回截止时间
        String deadline = null;
        if (Boolean.TRUE.equals(timeVO.getTimeLimit()) && currentUser != null) {
            AssessmentSessionVO session = assessmentSessionDomainService
                    .getOrCreateSession(currentUser.getId(), assessmentTimeId);
            deadline = session.getDeadline().toString();
        }

        return UserQuestionListResponse.builder()
                .questions(PageDTO.from(dtoPage))
                .deadline(deadline)
                .build();
    }

    @Override
    public AssessmentQuestionDTO getQuestionDetailForUser(Long id) {
        AssessmentQuestionVO vo = assessmentQuestionDomainService.getQuestionById(id);

        UserVO currentUser = UserCTX.getCurrentUser();

        if (currentUser != null) {
            // CANDIDATE 权限校验
            RoleType roleType = RoleType.fromName(currentUser.getRoleName());
            if (roleType == RoleType.CANDIDATE) {
                AssessmentTimeVO timeVO = assessmentTimeDomainService.getById(vo.getAssessmentTimeId())
                        .orElseThrow(() -> new IllegalArgumentException("考核时间不存在"));
                if (!currentUser.getDirection().equals(timeVO.getDirection())) {
                    throw new SecurityException("无权查看该题目");
                }
                Integer userEnrollmentYear = GradeCalculator.resolveAssessmentYear(
                        currentUser.getStudentId(),
                        currentUser.getAssessmentGradeYear());
                if (userEnrollmentYear != null && !userEnrollmentYear.equals(timeVO.getGrade())) {
                    throw new SecurityException("无权查看该题目");
                }
            }
        }

        // 返回完整详情（包含content）
        return assessmentQuestionConverter.convertToDTO(vo);
    }

    @Override
    @Transactional
    public void updateAttachment(Long questionId, Long fileId) {
        AssessmentQuestionVO question = assessmentQuestionDomainService.getQuestionById(questionId);

        FileVO fileVO = fileDomainService.getFileById(fileId);
        if (fileVO == null) {
            throw new DataNotFound("文件不存在");
        }
        if (fileVO.getType() != FileType.ASSESSMENT_ATTACHMENT) {
            throw new BadRequest("文件类型不匹配，期望 ASSESSMENT_ATTACHMENT");
        }

        assessmentQuestionDomainService.updateAttachment(question, fileVO);
        log.info("题目附件更新成功 - questionId={}, fileId={}", questionId, fileId);
    }
}
