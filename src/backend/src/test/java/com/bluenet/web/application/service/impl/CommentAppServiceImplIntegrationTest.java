package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.result.comment.CommentResult;
import com.bluenet.web.application.service.CommentAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.repository.CommentRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommentAppServiceImpl 集成测试。
 *
 * <p>
 * 按新测试策略：真实 Repository，使用测试夹具创建用户。本类验证应用服务层的编排、 事务边界与响应格式。
 * </p>
 */
@DisplayName("CommentAppServiceImpl 集成测试")
class CommentAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CommentAppService commentAppService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User createUser(String studentId) {
        return UserFixture.member(studentId).save(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("addComment: 应创建评论")
    void addComment_shouldCreateComment() {
        User user = createUser("2025001001");

        CommentResult result = commentAppService.addComment(user.getId(), 1L, "评论内容", new BigDecimal("8.5"));

        assertNotNull(result.id());
        assertEquals(1L, result.answerId());
        assertEquals(user.getId(), result.userId());
        assertEquals("评论内容", result.content());
        assertEquals(new BigDecimal("8.5"), result.score());
    }

    @Test
    @DisplayName("addComment: 重复评论同一答案应抛异常")
    void addComment_duplicate_shouldThrow() {
        User user = createUser("2025001002");
        commentAppService.addComment(user.getId(), 2L, "第一次", new BigDecimal("7.0"));

        assertThrows(
                BadRequest.class,
                () -> commentAppService.addComment(user.getId(), 2L, "第二次", new BigDecimal("8.0")));
    }

    @Test
    @DisplayName("listComments: 应返回评论列表并填充用户名")
    void listComments_shouldReturnCommentsWithUsername() {
        User user = createUser("2025001003");
        commentAppService.addComment(user.getId(), 3L, "评论", new BigDecimal("9.0"));

        List<CommentResult> results = commentAppService.listComments(3L);

        assertEquals(1, results.size());
        assertEquals("用户2025001003", results.get(0).username());
    }

    @Test
    @DisplayName("updateComment: 作者应能更新评论")
    void updateComment_author_shouldUpdate() {
        User user = createUser("2025001004");
        CommentResult created = commentAppService.addComment(user.getId(), 4L, "旧内容", new BigDecimal("7.0"));

        CommentResult updated = commentAppService
                .updateComment(user.getId(), created.id(), "新内容", new BigDecimal("9.0"));

        assertEquals("新内容", updated.content());
        assertEquals(new BigDecimal("9.0"), updated.score());
    }

    @Test
    @DisplayName("updateComment: 非作者应被禁止")
    void updateComment_nonAuthor_shouldThrow() {
        User author = createUser("2025001005");
        User other = createUser("2025001006");
        CommentResult created = commentAppService.addComment(author.getId(), 5L, "内容", new BigDecimal("7.0"));

        assertThrows(
                Forbidden.class,
                () -> commentAppService.updateComment(other.getId(), created.id(), "篡改", new BigDecimal("9.0")));
    }

    @Test
    @DisplayName("updateComment: 评论不存在应抛异常")
    void updateComment_notFound_shouldThrow() {
        User user = createUser("2025001007");
        assertThrows(
                DataNotFound.class,
                () -> commentAppService.updateComment(user.getId(), -1L, "内容", new BigDecimal("7.0")));
    }

    @Test
    @DisplayName("deleteComment: 作者应能删除评论")
    void deleteComment_author_shouldDelete() {
        User user = createUser("2025001008");
        CommentResult created = commentAppService.addComment(user.getId(), 6L, "内容", new BigDecimal("7.0"));

        commentAppService.deleteComment(user.getId(), created.id());

        assertTrue(commentRepository.findById(created.id()).isEmpty());
    }

    @Test
    @DisplayName("deleteComment: 非作者应被禁止")
    void deleteComment_nonAuthor_shouldThrow() {
        User author = createUser("2025001009");
        User other = createUser("2025001010");
        CommentResult created = commentAppService.addComment(author.getId(), 7L, "内容", new BigDecimal("7.0"));

        assertThrows(
                Forbidden.class,
                () -> commentAppService.deleteComment(other.getId(), created.id()));
    }

    @Test
    @DisplayName("deleteComment: 评论不存在应抛异常")
    void deleteComment_notFound_shouldThrow() {
        User user = createUser("2025001011");
        assertThrows(
                DataNotFound.class,
                () -> commentAppService.deleteComment(user.getId(), -1L));
    }
}
