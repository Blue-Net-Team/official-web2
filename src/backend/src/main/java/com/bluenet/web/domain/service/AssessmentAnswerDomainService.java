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
}
