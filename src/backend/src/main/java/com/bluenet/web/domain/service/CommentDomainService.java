package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.CommentVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 考核评论领域服务接口
 */
public interface CommentDomainService {

    /**
     * 添加评论（同一用户对同一答案只能评论一次）
     */
    CommentVO addComment(Long answerId, Long userId, String content, BigDecimal score);

    /**
     * 按答案查询所有评论
     */
    List<CommentVO> listCommentsByAnswerId(Long answerId);

    /**
     * 更新评论（仅限评论者本人）
     */
    CommentVO updateComment(Long commentId, Long userId, String content, BigDecimal score);

    /**
     * 删除评论（仅限评论者本人）
     */
    void deleteComment(Long commentId, Long userId);
}
