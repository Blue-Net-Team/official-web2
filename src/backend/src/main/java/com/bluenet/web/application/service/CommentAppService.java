package com.bluenet.web.application.service;

import com.bluenet.web.domain.model.vo.CommentVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 考核评论应用服务接口
 */
public interface CommentAppService {

    /**
     * 添加评论
     */
    CommentVO addComment(Long answerId, String content, BigDecimal score);

    /**
     * 按答案查询评论列表
     */
    List<CommentVO> listComments(Long answerId);

    /**
     * 更新评论
     */
    CommentVO updateComment(Long commentId, String content, BigDecimal score);

    /**
     * 删除评论
     */
    void deleteComment(Long commentId);
}
