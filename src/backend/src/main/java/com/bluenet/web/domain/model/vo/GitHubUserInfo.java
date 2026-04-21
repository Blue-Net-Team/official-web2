package com.bluenet.web.domain.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * GitHub OAuth 用户信息
 */
@Data
public class GitHubUserInfo {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @JsonProperty("id")
    private Long id;
    /**
     * GitHub 登录名。
     */
    @JsonProperty("login")
    private String login;
    /**
     * 业务对象名称。
     */
    @JsonProperty("name")
    private String name;
    /**
     * 用户邮箱地址。
     */
    @JsonProperty("email")
    private String email;
    /**
     * 第三方账号或展示层使用的头像访问地址。
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;
}
