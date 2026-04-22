package com.bluenet.web.domain.model.enumerate;

/**
 * 领域枚举的稳定业务值。
 *
 * <p>
 * 该值可被接口转换和持久化适配层使用，但领域层不依赖具体框架注解。
 * </p>
 */
public interface ValueEnum {
    String getValue();
}
