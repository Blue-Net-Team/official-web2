package com.bluenet.web.application.service;

import com.bluenet.web.application.CommentResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * 考核评论应用服务接口
 */
public interface CommentAppService {

    /**
     * 添加评论
     *
     * @param userId
     *            当前用户ID
     * @param answerId
     *            答案ID
     * @param content
     *            评论内容
     * @param score
     *            评分
     */
    CommentResult addComment(Long userId, Long answerId, String content, BigDecimal score);

    /**
     * 按答案查询评论列表
     */
    List<CommentResult> listComments(Long answerId);

    /**
     * 更新评论
     *
     * @param userId
     *            当前用户ID
     * @param commentId
     *            评论ID
     * @param content
     *            评论内容
     * @param score
     *            评分
     */
    CommentResult updateComment(Long userId, Long commentId, String content, BigDecimal score);

    /**
     * 删除评论
     *
     * @param userId
     *            当前用户ID
     * @param commentId
     *            评论ID
     */
    void deleteComment(Long userId, Long commentId);
}
