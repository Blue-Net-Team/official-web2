package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.Comment;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.CommentRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.dataobject.CommentDO;
import com.bluenet.web.infrastructure.repository.mapper.CommentMapper;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommentRepositoryImpl 集成测试。
 */
@DisplayName("CommentRepositoryImpl 集成测试")
class CommentRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final AtomicLong userCounter = new AtomicLong(1);

    private Long createUser() {
        long seq = userCounter.getAndIncrement();
        String studentId = String.format("2025%05d", seq);
        User user = User.create(
                studentId,
                studentId + "@example.com",
                roleMapper.selectByName(RoleType.MEMBER.getName()).getId(),
                passwordEncoder.encode("pwd"),
                "用户" + studentId,
                "昵称" + studentId,
                null,
                null,
                null,
                null,
                Gender.UNKNOWN,
                null,
                null,
                null,
                null,
                null,
                "REF" + seq,
                null);
        userRepository.save(user);
        return user.getId();
    }

    private Comment createComment(Long answerId, Long userId, String content, BigDecimal score) {
        Comment comment = Comment.create(answerId, userId, content, score, false);
        commentRepository.save(comment);
        return comment;
    }

    @Test
    @DisplayName("save: 新评论应插入并回写ID")
    void save_newComment_shouldInsertAndReturnId() {
        Long userId = createUser();
        Comment comment = createComment(1L, userId, "评论内容", new BigDecimal("8.5"));

        assertNotNull(comment.getId());
        CommentDO dataObject = commentMapper.selectById(comment.getId());
        assertNotNull(dataObject);
        assertEquals("评论内容", dataObject.getContent());
        assertEquals(0, new BigDecimal("8.5").compareTo(dataObject.getScore()));
    }

    @Test
    @DisplayName("save: 已有评论应更新字段")
    void save_existingComment_shouldUpdateFields() {
        Long userId = createUser();
        Comment comment = createComment(1L, userId, "旧内容", new BigDecimal("7.0"));
        comment.update(userId, "新内容", new BigDecimal("9.0"));

        commentRepository.save(comment);

        CommentDO updated = commentMapper.selectById(comment.getId());
        assertEquals("新内容", updated.getContent());
        assertEquals(0, new BigDecimal("9.0").compareTo(updated.getScore()));
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        Long userId = createUser();
        Comment comment = createComment(1L, userId, "内容", new BigDecimal("6.0"));

        Optional<Comment> found = commentRepository.findById(comment.getId());
        assertTrue(found.isPresent());
        assertEquals(comment.getContent(), found.get().getContent());

        assertTrue(commentRepository.findById(-1L).isEmpty());
    }

    @Test
    @DisplayName("findByAnswerId: 应按答案ID查询所有评论")
    void findByAnswerId_shouldReturnComments() {
        Long user1 = createUser();
        Long user2 = createUser();
        Long user3 = createUser();
        Comment comment1 = createComment(10L, user1, "评论1", new BigDecimal("5.0"));
        Comment comment2 = createComment(10L, user2, "评论2", new BigDecimal("6.0"));
        createComment(20L, user3, "其他答案评论", new BigDecimal("7.0"));

        List<Comment> comments = commentRepository.findByAnswerId(10L);

        assertEquals(2, comments.size());
        assertTrue(comments.stream().anyMatch(c -> c.getId().equals(comment1.getId())));
        assertTrue(comments.stream().anyMatch(c -> c.getId().equals(comment2.getId())));
    }

    @Test
    @DisplayName("existsByAnswerIdAndUserId: 应正确判断用户是否已评论")
    void existsByAnswerIdAndUserId_shouldWork() {
        Long userId = createUser();
        createComment(30L, userId, "已评论", new BigDecimal("8.0"));

        assertTrue(commentRepository.existsByAnswerIdAndUserId(30L, userId));
        assertFalse(commentRepository.existsByAnswerIdAndUserId(30L, -1L));
        assertFalse(commentRepository.existsByAnswerIdAndUserId(31L, userId));
    }

    @Test
    @DisplayName("deleteById: 应删除评论")
    void deleteById_shouldRemoveComment() {
        Long userId = createUser();
        Comment comment = createComment(1L, userId, "待删除", new BigDecimal("8.0"));

        commentRepository.deleteById(comment.getId());

        assertNull(commentMapper.selectById(comment.getId()));
    }
}
