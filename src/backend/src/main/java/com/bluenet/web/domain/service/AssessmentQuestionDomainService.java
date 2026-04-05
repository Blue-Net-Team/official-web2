package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.FileVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 题目领域服务接口
 */
public interface AssessmentQuestionDomainService {

    /**
     * 更新题目附件
     *
     * @param question
     *            题目VO
     * @param file
     *            文件信息
     */
    void updateAttachment(AssessmentQuestionVO question, FileVO file);

    /**
     * 根据题目ID获取题目信息
     *
     * @param questionId
     *            题目ID
     * @return 题目VO
     */
    AssessmentQuestionVO getQuestionById(Long questionId);

    /**
     * 创建考题
     *
     * @param question
     *            考题VO
     * @return 创建后的考题VO
     */
    AssessmentQuestionVO createQuestion(AssessmentQuestionVO question);

    /**
     * 更新考题
     *
     * @param question
     *            考题VO
     * @return 更新后的考题VO
     */
    AssessmentQuestionVO updateQuestion(AssessmentQuestionVO question);

    /**
     * 删除考题
     *
     * @param id
     *            考题ID
     */
    void deleteQuestion(Long id);

    /**
     * 分页查询考题
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param pageable
     *            分页参数
     * @return 分页结果
     */
    Page<AssessmentQuestionVO> listQuestions(Long assessmentTimeId, Pageable pageable);
}
