package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.Comment;
import com.bluenet.web.domain.model.vo.CommentVO;
import com.bluenet.web.domain.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CommentDomainServiceImpl 单元测试。
 */
@DisplayName("CommentDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class CommentDomainServiceImplTest {

    private static final Long COMMENT_ID = 100L;
    private static final Long ANSWER_ID = 10L;
    private static final Long USER_ID = 20L;
    private static final Long OTHER_USER_ID = 30L;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentDomainServiceImpl commentDomainService;

    @Test
    @DisplayName("添加评论：新用户对答案首次评论应成功")
    void addComment_firstTime_shouldSaveAndReturnVO() {
        when(commentRepository.existsByAnswerIdAndUserId(ANSWER_ID, USER_ID)).thenReturn(false);
        doAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(COMMENT_ID);
            return null;
        }).when(commentRepository).save(any(Comment.class));

        CommentVO result = commentDomainService.addComment(ANSWER_ID, USER_ID, "完成度不错", BigDecimal.valueOf(8));

        assertEquals(COMMENT_ID, result.getId());
        assertEquals(ANSWER_ID, result.getAnswerId());
        assertEquals(USER_ID, result.getUserId());
        assertEquals("完成度不错", result.getContent());
        assertEquals(BigDecimal.valueOf(8), result.getScore());
        assertNotNull(result.getCommentTime());
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertEquals(ANSWER_ID, captor.getValue().getAnswerId());
        assertEquals(USER_ID, captor.getValue().getUserId());
    }

    @Test
    @DisplayName("添加评论：同一用户对同一答案重复评论应拒绝")
    void addComment_duplicate_shouldThrowBadRequest() {
        when(commentRepository.existsByAnswerIdAndUserId(ANSWER_ID, USER_ID)).thenReturn(true);

        assertThrows(
                BadRequest.class,
                () -> commentDomainService.addComment(ANSWER_ID, USER_ID, "重复评论", BigDecimal.ONE));
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("查询评论列表：应按答案ID返回所有评论")
    void listCommentsByAnswerId_shouldReturnRepositoryResults() {
        CommentVO vo = CommentVO.builder()
                .id(COMMENT_ID)
                .answerId(ANSWER_ID)
                .userId(USER_ID)
                .content("评论内容")
                .score(BigDecimal.valueOf(7))
                .commentTime(LocalDateTime.now())
                .build();
        when(commentRepository.findByAnswerId(ANSWER_ID)).thenReturn(List.of(vo));

        List<CommentVO> result = commentDomainService.listCommentsByAnswerId(ANSWER_ID);

        assertEquals(1, result.size());
        assertEquals(COMMENT_ID, result.get(0).getId());
        assertEquals("评论内容", result.get(0).getContent());
    }

    @Test
    @DisplayName("更新评论：评论所有者修改内容应成功")
    void updateComment_owner_shouldUpdateAndReturnVO() {
        Comment existing = Comment
                .reconstruct(COMMENT_ID, ANSWER_ID, USER_ID, "旧内容", BigDecimal.valueOf(5), LocalDateTime.now());
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(existing));

        CommentVO result = commentDomainService.updateComment(COMMENT_ID, USER_ID, "新内容", BigDecimal.valueOf(7));

        assertEquals("新内容", result.getContent());
        assertEquals(BigDecimal.valueOf(7), result.getScore());
        verify(commentRepository).update(any(Comment.class));
    }

    @Test
    @DisplayName("更新评论：评论不存在应抛出DataNotFound")
    void updateComment_notFound_shouldThrowDataNotFound() {
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

        assertThrows(
                DataNotFound.class,
                () -> commentDomainService.updateComment(COMMENT_ID, USER_ID, "内容", BigDecimal.ONE));
        verify(commentRepository, never()).update(any());
    }

    @Test
    @DisplayName("更新评论：非所有者修改应抛出Forbidden")
    void updateComment_notOwner_shouldThrowForbidden() {
        Comment existing = Comment
                .reconstruct(COMMENT_ID, ANSWER_ID, OTHER_USER_ID, "内容", BigDecimal.valueOf(5), LocalDateTime.now());
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(existing));

        assertThrows(
                Forbidden.class,
                () -> commentDomainService.updateComment(COMMENT_ID, USER_ID, "新内容", BigDecimal.ONE));
        verify(commentRepository, never()).update(any());
    }

    @Test
    @DisplayName("删除评论：评论所有者删除应成功")
    void deleteComment_owner_shouldDelete() {
        Comment existing = Comment
                .reconstruct(COMMENT_ID, ANSWER_ID, USER_ID, "内容", BigDecimal.valueOf(5), LocalDateTime.now());
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(existing));

        commentDomainService.deleteComment(COMMENT_ID, USER_ID);

        verify(commentRepository).deleteById(COMMENT_ID);
    }

    @Test
    @DisplayName("删除评论：评论不存在应抛出DataNotFound")
    void deleteComment_notFound_shouldThrowDataNotFound() {
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

        assertThrows(DataNotFound.class, () -> commentDomainService.deleteComment(COMMENT_ID, USER_ID));
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("删除评论：非所有者删除应抛出Forbidden")
    void deleteComment_notOwner_shouldThrowForbidden() {
        Comment existing = Comment
                .reconstruct(COMMENT_ID, ANSWER_ID, OTHER_USER_ID, "内容", BigDecimal.valueOf(5), LocalDateTime.now());
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(existing));

        assertThrows(Forbidden.class, () -> commentDomainService.deleteComment(COMMENT_ID, USER_ID));
        verify(commentRepository, never()).deleteById(any());
    }
}
