package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Forbidden;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comment 领域实体单元测试。
 */
@DisplayName("Comment 领域实体测试")
class CommentTest {

    @Test
    @DisplayName("create: 应创建新评论")
    void create_shouldCreateComment() {
        Comment comment = Comment.create(1L, 2L, "内容", new BigDecimal("8.5"), false);

        assertNull(comment.getId());
        assertEquals(1L, comment.getAnswerId());
        assertEquals(2L, comment.getUserId());
        assertEquals("内容", comment.getContent());
        assertEquals(new BigDecimal("8.5"), comment.getScore());
        assertNotNull(comment.getCommentTime());
    }

    @Test
    @DisplayName("create: 已评论过同一答案应抛异常")
    void create_alreadyCommented_shouldThrow() {
        assertThrows(
                BadRequest.class,
                () -> Comment.create(1L, 2L, "内容", new BigDecimal("8.5"), true));
    }

    @Test
    @DisplayName("update: 作者应能更新内容和分数")
    void update_author_shouldUpdateFields() {
        Comment comment = Comment.create(1L, 2L, "旧内容", new BigDecimal("7.0"), false);
        comment.update(2L, "新内容", new BigDecimal("9.0"));

        assertEquals("新内容", comment.getContent());
        assertEquals(new BigDecimal("9.0"), comment.getScore());
    }

    @Test
    @DisplayName("update: 非作者应被禁止")
    void update_nonAuthor_shouldThrow() {
        Comment comment = Comment.create(1L, 2L, "内容", new BigDecimal("7.0"), false);

        assertThrows(
                Forbidden.class,
                () -> comment.update(3L, "新内容", new BigDecimal("9.0")));
    }

    @Test
    @DisplayName("update: null 字段不应覆盖原值")
    void update_nullFields_shouldPreserveOriginal() {
        Comment comment = Comment.create(1L, 2L, "内容", new BigDecimal("7.0"), false);
        comment.update(2L, null, null);

        assertEquals("内容", comment.getContent());
        assertEquals(new BigDecimal("7.0"), comment.getScore());
    }

    @Test
    @DisplayName("delete: 作者应能通过校验")
    void delete_author_shouldPass() {
        Comment comment = Comment.create(1L, 2L, "内容", new BigDecimal("7.0"), false);
        assertDoesNotThrow(() -> comment.delete(2L));
    }

    @Test
    @DisplayName("delete: 非作者应被禁止")
    void delete_nonAuthor_shouldThrow() {
        Comment comment = Comment.create(1L, 2L, "内容", new BigDecimal("7.0"), false);
        assertThrows(Forbidden.class, () -> comment.delete(3L));
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveFields() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Comment comment = Comment.reconstruct(10L, 1L, 2L, "内容", new BigDecimal("8.0"), now);

        assertEquals(10L, comment.getId());
        assertEquals(1L, comment.getAnswerId());
        assertEquals(2L, comment.getUserId());
        assertEquals("内容", comment.getContent());
        assertEquals(new BigDecimal("8.0"), comment.getScore());
        assertEquals(now, comment.getCommentTime());
    }
}
