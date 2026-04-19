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
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.util.GradeCalculator;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.domain.model.vo.evaluation.AlgorithmContent;
import com.bluenet.web.domain.model.vo.evaluation.QuestionContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;

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
        validateQuestionContent(request.getQuestionType(), questionContent);

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
        QuestionType questionType = request.getQuestionType() != null
                ? request.getQuestionType()
                : existing.getQuestionType();
        validateQuestionContent(questionType, updatedContent);

        AssessmentQuestionVO vo = AssessmentQuestionVO.builder()
                .id(id)
                .assessmentTimeId(existing.getAssessmentTimeId())
                .questionNo(request.getQuestionNo() != null ? request.getQuestionNo() : existing.getQuestionNo())
                .questionType(questionType)
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

        // 用户端算法题不能提前暴露正式判题用例，避免提交前泄露隐藏测试点。
        return assessmentQuestionConverter.convertToDTO(sanitizeQuestionForUser(vo));
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

    private void validateQuestionContent(QuestionType questionType, QuestionContent content) {
        if (questionType != QuestionType.ALGORITHM) {
            return;
        }
        if (!(content instanceof AlgorithmContent algorithmContent)) {
            throw new IllegalArgumentException("算法题内容不能为空");
        }
        validateStarterCode(algorithmContent.getStarterCode());
        validateTestCases(algorithmContent.getTestCases(), true, "正式测试用例");
        validateTestCases(algorithmContent.getRunTestCases(), false, "默认运行测试用例");
    }

    private void validateStarterCode(Map<String, String> starterCode) {
        if (starterCode == null || starterCode.isEmpty()) {
            throw new IllegalArgumentException("算法题至少需要配置一个语言模板");
        }
        boolean hasUsableTemplate = starterCode.entrySet()
                .stream()
                .anyMatch(entry -> hasText(entry.getKey()) && hasText(entry.getValue()));
        if (!hasUsableTemplate) {
            throw new IllegalArgumentException("算法题至少需要配置一个有效语言模板");
        }
    }

    private void validateTestCases(List<AlgorithmContent.TestCase> testCases, boolean required, String label) {
        if (testCases == null || testCases.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException("算法题至少需要配置一个" + label);
            }
            return;
        }
        for (int i = 0; i < testCases.size(); i++) {
            AlgorithmContent.TestCase testCase = testCases.get(i);
            if (testCase == null || !hasText(testCase.getInput()) || !hasText(testCase.getExpectedOutput())) {
                throw new IllegalArgumentException(label + "第" + (i + 1) + "个用例必须包含输入和期望输出");
            }
        }
    }

    private AssessmentQuestionVO sanitizeQuestionForUser(AssessmentQuestionVO question) {
        if (question.getQuestionType() != QuestionType.ALGORITHM
                || !(question.getContent()instanceof AlgorithmContent algorithmContent)) {
            return question;
        }
        AlgorithmContent sanitizedContent = new AlgorithmContent();
        sanitizedContent.setContent(algorithmContent.getContent());
        sanitizedContent.setInputDescription(algorithmContent.getInputDescription());
        sanitizedContent.setOutputDescription(algorithmContent.getOutputDescription());
        sanitizedContent.setConstraints(algorithmContent.getConstraints());
        sanitizedContent.setExamples(algorithmContent.getExamples());
        sanitizedContent.setRunTestCases(algorithmContent.getRunTestCases());
        sanitizedContent.setStarterCode(algorithmContent.getStarterCode());
        sanitizedContent.setTimeLimit(algorithmContent.getTimeLimit());
        sanitizedContent.setMemoryLimit(algorithmContent.getMemoryLimit());
        // 正式 testCases 仅供判题使用，用户端题目详情不直接返回。
        sanitizedContent.setTestCases(null);

        return AssessmentQuestionVO.builder()
                .id(question.getId())
                .assessmentTimeId(question.getAssessmentTimeId())
                .questionNo(question.getQuestionNo())
                .questionType(question.getQuestionType())
                .title(question.getTitle())
                .content(sanitizedContent)
                .attachmentId(question.getAttachmentId())
                .score(question.getScore())
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
