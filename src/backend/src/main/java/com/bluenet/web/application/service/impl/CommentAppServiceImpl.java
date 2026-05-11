package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.service.CommentAppService;
import com.bluenet.web.domain.model.vo.CommentVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.service.CommentDomainService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
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
    public CommentVO addComment(Long answerId, String content, BigDecimal score) {
        UserVO currentUser = getCurrentUser();
        return commentDomainService.addComment(answerId, currentUser.getId(), content, score);
    }

    @Override
    public List<CommentVO> listComments(Long answerId) {
        return commentDomainService.listCommentsByAnswerId(answerId);
    }

    @Override
    public CommentVO updateComment(Long commentId, String content, BigDecimal score) {
        UserVO currentUser = getCurrentUser();
        return commentDomainService.updateComment(commentId, currentUser.getId(), content, score);
    }

    @Override
    public void deleteComment(Long commentId) {
        UserVO currentUser = getCurrentUser();
        commentDomainService.deleteComment(commentId, currentUser.getId());
    }

    private UserVO getCurrentUser() {
        UserVO user = UserCTX.getCurrentUser();
        if (user == null) {
            throw new SecurityException("未登录");
        }
        return user;
    }
}
