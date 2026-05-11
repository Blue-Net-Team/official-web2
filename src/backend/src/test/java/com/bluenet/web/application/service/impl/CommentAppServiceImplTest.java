package com.bluenet.web.application.service.impl;

import com.bluenet.web.domain.model.vo.CommentVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.service.CommentDomainService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CommentAppServiceImpl 单元测试。
 */
@DisplayName("CommentAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class CommentAppServiceImplTest {

    private static final Long COMMENT_ID = 100L;
    private static final Long ANSWER_ID = 10L;
    private static final Long USER_ID = 20L;

    @Mock
    private CommentDomainService commentDomainService;

    @InjectMocks
    private CommentAppServiceImpl commentAppService;

    @Test
    @DisplayName("添加评论：已登录用户应委托领域服务并返回结果")
    void addComment_loggedIn_shouldDelegateAndReturnVO() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser());
            when(commentDomainService.addComment(ANSWER_ID, USER_ID, "内容", BigDecimal.valueOf(8)))
                    .thenReturn(createCommentVO());

            CommentVO result = commentAppService.addComment(ANSWER_ID, "内容", BigDecimal.valueOf(8));

            assertEquals(COMMENT_ID, result.getId());
            verify(commentDomainService).addComment(ANSWER_ID, USER_ID, "内容", BigDecimal.valueOf(8));
        }
    }

    @Test
    @DisplayName("添加评论：未登录应抛出安全异常")
    void addComment_notLoggedIn_shouldThrowSecurityException() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(null);

            assertThrows(SecurityException.class, () -> commentAppService.addComment(ANSWER_ID, "内容", BigDecimal.ONE));
            verifyNoInteractions(commentDomainService);
        }
    }

    @Test
    @DisplayName("查询评论列表：应委托领域服务并返回结果")
    void listComments_shouldDelegateAndReturnList() {
        when(commentDomainService.listCommentsByAnswerId(ANSWER_ID))
                .thenReturn(List.of(createCommentVO()));

        List<CommentVO> result = commentAppService.listComments(ANSWER_ID);

        assertEquals(1, result.size());
        assertEquals(COMMENT_ID, result.get(0).getId());
        verify(commentDomainService).listCommentsByAnswerId(ANSWER_ID);
    }

    @Test
    @DisplayName("更新评论：已登录用户应委托领域服务并返回结果")
    void updateComment_loggedIn_shouldDelegateAndReturnVO() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser());
            when(commentDomainService.updateComment(COMMENT_ID, USER_ID, "新内容", BigDecimal.valueOf(9)))
                    .thenReturn(createCommentVO());

            CommentVO result = commentAppService.updateComment(COMMENT_ID, "新内容", BigDecimal.valueOf(9));

            assertEquals(COMMENT_ID, result.getId());
            verify(commentDomainService).updateComment(COMMENT_ID, USER_ID, "新内容", BigDecimal.valueOf(9));
        }
    }

    @Test
    @DisplayName("更新评论：未登录应抛出安全异常")
    void updateComment_notLoggedIn_shouldThrowSecurityException() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(null);

            assertThrows(
                    SecurityException.class,
                    () -> commentAppService.updateComment(COMMENT_ID, "新内容", BigDecimal.ONE));
            verifyNoInteractions(commentDomainService);
        }
    }

    @Test
    @DisplayName("删除评论：已登录用户应委托领域服务")
    void deleteComment_loggedIn_shouldDelegate() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(createUser());
            doNothing().when(commentDomainService).deleteComment(COMMENT_ID, USER_ID);

            commentAppService.deleteComment(COMMENT_ID);

            verify(commentDomainService).deleteComment(COMMENT_ID, USER_ID);
        }
    }

    @Test
    @DisplayName("删除评论：未登录应抛出安全异常")
    void deleteComment_notLoggedIn_shouldThrowSecurityException() {
        try (MockedStatic<UserCTX> mockedUserCTX = mockStatic(UserCTX.class)) {
            mockedUserCTX.when(UserCTX::getCurrentUser).thenReturn(null);

            assertThrows(SecurityException.class, () -> commentAppService.deleteComment(COMMENT_ID));
            verifyNoInteractions(commentDomainService);
        }
    }

    private UserVO createUser() {
        return UserVO.builder()
                .id(USER_ID)
                .username("测试用户")
                .build();
    }

    private CommentVO createCommentVO() {
        return CommentVO.builder()
                .id(COMMENT_ID)
                .answerId(ANSWER_ID)
                .userId(USER_ID)
                .content("评论内容")
                .score(BigDecimal.valueOf(8))
                .commentTime(LocalDateTime.now())
                .build();
    }
}
