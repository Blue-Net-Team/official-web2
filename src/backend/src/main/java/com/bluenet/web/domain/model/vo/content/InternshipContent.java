package com.bluenet.web.domain.model.vo.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 实习经历内容
 * <p>
 * 存储在 tb_user_experience.content 字段中的JSON结构。
 * </p>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InternshipContent {
    /**
     * 实习岗位
     */
    private String position;

    /**
     * 描述
     */
    private String description;

    /**
     * 成就列表
     */
    private List<String> achievements;

    /**
     * 状态（active/ended）
     */
    private String status;
}
