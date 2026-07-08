package com.bluenet.web.application.result.college;

/**
 * 学院聚合的应用层结果对象。
 * <p>
 * 封装了学院相关操作返回给 API 层的数据。
 * </p>
 */
public record CollegeResult(
        /** 唯一标识 */
        Long id,
        /** 名称 */
        String name) {
}
