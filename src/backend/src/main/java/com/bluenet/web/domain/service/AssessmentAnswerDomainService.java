package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.AssessmentAnswerVO;
import com.bluenet.web.domain.model.vo.FileVO;

/**
 * 答题领域服务接口
 */
public interface AssessmentAnswerDomainService {

    /**
     * 更新答题工作文件
     *
     * @param answer
     *            答题VO
     * @param file
     *            文件信息
     */
    void updateWorkFile(AssessmentAnswerVO answer, FileVO file);

    /**
     * 根据答题ID获取答题信息
     *
     * @param answerId
     *            答题ID
     * @return 答题VO
     */
    AssessmentAnswerVO getAnswerById(Long answerId);

    /**
     * 创建答案（含重复提交检查）
     *
     * @param answer
     *            答案VO
     * @return 创建后的答案VO
     */
    AssessmentAnswerVO createAnswer(AssessmentAnswerVO answer);

    /**
     * 更新答案（重新提交）
     *
     * @param answer
     *            已有答案VO
     * @param fileId
     *            新的文件ID（可为null）
     * @param content
     *            新的内容（可为null）
     */
    void updateAnswer(AssessmentAnswerVO answer, Long fileId, String content);
}
