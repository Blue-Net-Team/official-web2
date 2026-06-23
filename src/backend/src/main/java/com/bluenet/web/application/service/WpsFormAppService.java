package com.bluenet.web.application.service;

/**
 * WPS 智能表单应用服务接口。
 */
public interface WpsFormAppService {

    /**
     * 通过 WPS 表单提交数据创建用户。
     * <p>
     * 解析方向文本，生成随机密码，创建 MEMBER 角色用户， 并将密码通过邮件发送到用户邮箱。
     * </p>
     *
     * @param studentId
     *            学号
     * @param username
     *            姓名
     * @param email
     *            邮箱
     * @param directionText
     *            方向（中文，如"视觉"、"计算机视觉"）
     * @param major
     *            专业
     * @param collegeText
     *            学院（中文文本）
     * @param genderText
     *            性别（中文文本）
     */
    void createUserFromWpsForm(String studentId, String username, String email,
            String directionText, String major,
            String collegeText, String genderText);
}
