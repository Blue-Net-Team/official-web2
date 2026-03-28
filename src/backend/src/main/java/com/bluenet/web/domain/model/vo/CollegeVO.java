package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 学院值对象
 * <p>
 * 用于领域层传递学院信息
 * </p>
 */
@Data
@AllArgsConstructor
@Builder
public class CollegeVO {
    /**
     * 学院ID
     */
    private Long id;

    /**
     * 学院名称
     */
    private String name;
}
