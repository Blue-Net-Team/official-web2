package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapper 专用数据对象，只承载数据库表字段，避免持久层依赖领域实体行为。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_comment")
public class CommentDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
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

    /**
     * 评论者用户名，关联查询时填充。
     */
    @TableField(exist = false)
    private String username;
}
