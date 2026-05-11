package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.Comment;
import com.bluenet.web.domain.model.vo.CommentVO;
import com.bluenet.web.domain.repository.CommentRepository;
import com.bluenet.web.domain.service.CommentDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentDomainServiceImpl implements CommentDomainService {
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentVO addComment(Long answerId, Long userId, String content, BigDecimal score) {
        log.info("add comment for answer {} by user {}", answerId, userId);
        if (commentRepository.existsByAnswerIdAndUserId(answerId, userId)) {
            throw new BadRequest("您已对该答案发表过评论，不可重复评论");
        }
        Comment comment = Comment.create(answerId, userId, content, score);
        commentRepository.save(comment);
        return convertToVO(comment);
    }

    @Override
    public List<CommentVO> listCommentsByAnswerId(Long answerId) {
        return commentRepository.findByAnswerId(answerId);
    }

    @Override
    @Transactional
    public CommentVO updateComment(Long commentId, Long userId, String content, BigDecimal score) {
        log.info("update comment {} by user {}", commentId, userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFound("评论不存在"));
        if (!comment.getUserId().equals(userId)) {
            throw new Forbidden("只能修改自己的评论");
        }
        comment.update(content, score);
        commentRepository.update(comment);
        return convertToVO(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        log.info("delete comment {} by user {}", commentId, userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFound("评论不存在"));
        if (!comment.getUserId().equals(userId)) {
            throw new Forbidden("只能删除自己的评论");
        }
        commentRepository.deleteById(commentId);
    }

    private CommentVO convertToVO(Comment comment) {
        return CommentVO.builder()
                .id(comment.getId())
                .answerId(comment.getAnswerId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .score(comment.getScore())
                .commentTime(comment.getCommentTime())
                .build();
    }
}
