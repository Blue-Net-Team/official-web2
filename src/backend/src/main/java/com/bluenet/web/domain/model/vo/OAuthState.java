package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth state 参数信息，用于区分登录和绑定流程
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuthState {

    /**
     * 流程类型: "login" 或 "bind"
     */
    private String type;

    /**
     * 绑定流程中的用户ID（登录流程为 null）
     */
    private Long userId;
}
