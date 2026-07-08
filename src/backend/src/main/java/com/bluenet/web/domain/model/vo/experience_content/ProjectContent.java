package com.bluenet.web.domain.model.vo.experience_content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 项目经历内容
 * <p>
 * 存储在 tb_user_experience.content 字段中的JSON结构。
 * </p>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectContent {
    /**
     * 角色
     */
    private String role;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 技术栈
     */
    private List<String> techStack;

    /**
     * 演示链接
     */
    /**
     * 项目演示或在线预览地址。
     */
    @JsonProperty("demoUrl")
    private String demoUrl;
}
