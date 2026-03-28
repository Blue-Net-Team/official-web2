package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.domain.model.vo.FileVO;

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
}
