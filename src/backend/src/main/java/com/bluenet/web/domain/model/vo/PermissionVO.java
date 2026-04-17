package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限值对象
 * <p>
 * 用于领域层传递权限信息
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionVO {
    /**
     * 权限ID
     */
    private Long id;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限标识符（如 assessment:create）
     */
    private String value;

    /**
     * 接口URL（可选）
     */
    private String url;

    /**
     * HTTP方法（可选）
     */
    private String method;
}
