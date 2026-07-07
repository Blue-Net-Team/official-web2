package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Forbidden;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 考核作答记录标识。
     */
    private Long answerId;
    /**
     * 关联用户标识。
     */
    private Long userId;
    /**
     * 正文内容、题目内容或结构化配置内容。
     */
    private String content;
    /**
     * 答案、题目或评审记录在考核中的得分。
     */
    private BigDecimal score;
    /**
     * 评价或留言提交时间。
     */
    private LocalDateTime commentTime;

    private Comment(Long id, Long answerId, Long userId, String content, BigDecimal score,
            LocalDateTime commentTime) {
        this.id = id;
        this.answerId = answerId;
        this.userId = userId;
        this.content = content;
        this.score = score;
        this.commentTime = commentTime;
    }

    /**
     * 构造新评论
     *
     * @param alreadyCommented
     *            当前用户是否已对该答案发表过评论
     */
    public static Comment create(Long answerId, Long userId, String content, BigDecimal score,
            boolean alreadyCommented) {
        if (alreadyCommented) {
            throw new BadRequest("您已对该答案发表过评论，不可重复评论");
        }
        return new Comment(null, answerId, userId, content, score, LocalDateTime.now());
    }

    /**
     * 从数据库重建
     */
    public static Comment reconstruct(Long id, Long answerId, Long userId, String content, BigDecimal score,
            LocalDateTime commentTime) {
        return new Comment(id, answerId, userId, content, score, commentTime);
    }

    private void ensureAuthor(Long actingUserId) {
        if (!userId.equals(actingUserId)) {
            throw new Forbidden("只能操作自己的评论");
        }
    }

    /**
     * 更新评论内容（仅限评论者本人）
     */
    public void update(Long actingUserId, String content, BigDecimal score) {
        ensureAuthor(actingUserId);
        if (content != null) {
            this.content = content;
        }
        if (score != null) {
            this.score = score;
        }
    }

    /**
     * 删除评论校验（仅限评论者本人）
     */
    public void delete(Long actingUserId) {
        ensureAuthor(actingUserId);
    }
}
