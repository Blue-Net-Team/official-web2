package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.CommentResult;
import com.bluenet.web.application.service.CommentAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Comment;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.repository.CommentRepository;
import com.bluenet.web.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentAppServiceImpl implements CommentAppService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResult addComment(Long userId, Long answerId, String content, BigDecimal score) {
        log.info("add comment for answer {} by user {}", answerId, userId);
        boolean alreadyCommented = commentRepository.existsByAnswerIdAndUserId(answerId, userId);
        Comment comment = Comment.create(answerId, userId, content, score, alreadyCommented);
        commentRepository.save(comment);
        return toResult(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResult> listComments(Long answerId) {
        List<Comment> comments = commentRepository.findByAnswerId(answerId);
        Map<Long, String> usernameMap = fetchUsernameMap(comments);
        return comments.stream()
                .map(comment -> toResult(comment, usernameMap.get(comment.getUserId())))
                .toList();
    }

    @Override
    @Transactional
    public CommentResult updateComment(Long userId, Long commentId, String content, BigDecimal score) {
        log.info("update comment {} by user {}", commentId, userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFound("评论不存在"));
        comment.update(userId, content, score);
        commentRepository.save(comment);
        return toResult(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        log.info("delete comment {} by user {}", commentId, userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFound("评论不存在"));
        comment.delete(userId);
        commentRepository.deleteById(commentId);
    }

    private Map<Long, String> fetchUsernameMap(List<Comment> comments) {
        Set<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .filter(userId -> userId != null)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userIds.stream()
                .map(userRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }

    private CommentResult toResult(Comment comment) {
        return toResult(comment, null);
    }

    private CommentResult toResult(Comment comment, String username) {
        return CommentResult.builder()
                .id(comment.getId())
                .answerId(comment.getAnswerId())
                .userId(comment.getUserId())
                .username(username)
                .content(comment.getContent())
                .score(comment.getScore())
                .commentTime(comment.getCommentTime())
                .build();
    }
}
