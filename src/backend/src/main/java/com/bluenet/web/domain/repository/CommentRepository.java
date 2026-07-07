package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Comment;

import java.util.List;
import java.util.Optional;

/**
 * 考核评论仓储接口
 */
public interface CommentRepository {

    /**
     * 保存评论（新增或更新）
     */
    void save(Comment comment);

    /**
     * 按主键查询评论
     */
    Optional<Comment> findById(Long id);

    /**
     * 按答案查询所有评论
     */
    List<Comment> findByAnswerId(Long answerId);

    /**
     * 判断指定用户是否已对某答案评论
     */
    boolean existsByAnswerIdAndUserId(Long answerId, Long userId);

    /**
     * 删除评论
     */
    void deleteById(Long id);
}
