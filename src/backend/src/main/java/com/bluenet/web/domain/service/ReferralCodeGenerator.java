package com.bluenet.web.domain.service;

/**
 * 内推码生成服务接口
 * <p>
 * 负责生成唯一的8位大写字母+数字内推码
 * </p>
 */
public interface ReferralCodeGenerator {

    /**
     * 生成唯一的内推码
     *
     * @return 8位大写字母+数字组成的内推码
     */
    String generate();

    /**
     * 验证内推码格式是否有效
     *
     * @param code
     *            待验证的内推码
     * @return 如果格式有效返回 true，否则返回 false
     */
    boolean isValidFormat(String code);
}
