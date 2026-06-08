package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.service.CommentAppService;
import com.bluenet.web.domain.model.vo.CommentVO;
import com.bluenet.web.domain.service.CommentDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentAppServiceImpl implements CommentAppService {
    private final CommentDomainService commentDomainService;

    @Override
    public CommentVO addComment(Long userId, Long answerId, String content, BigDecimal score) {
        return commentDomainService.addComment(answerId, userId, content, score);
    }

    @Override
    public List<CommentVO> listComments(Long answerId) {
        return commentDomainService.listCommentsByAnswerId(answerId);
    }

    @Override
    public CommentVO updateComment(Long userId, Long commentId, String content, BigDecimal score) {
        return commentDomainService.updateComment(commentId, userId, content, score);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        commentDomainService.deleteComment(commentId, userId);
    }
}
