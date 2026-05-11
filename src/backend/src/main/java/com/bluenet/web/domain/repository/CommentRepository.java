package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Comment;
import com.bluenet.web.domain.model.vo.CommentVO;

import java.util.List;
import java.util.Optional;

/**
 * 考核评论仓储接口
 */
public interface CommentRepository {

    /**
     * 保存评论
     */
    void save(Comment comment);

    /**
     * 按主键查询评论
     */
    Optional<Comment> findById(Long id);

    /**
     * 按答案查询所有评论
     */
    List<CommentVO> findByAnswerId(Long answerId);

    /**
     * 判断指定用户是否已对某答案评论
     */
    boolean existsByAnswerIdAndUserId(Long answerId, Long userId);

    /**
     * 更新评论
     */
    void update(Comment comment);

    /**
     * 删除评论
     */
    void deleteById(Long id);
}
