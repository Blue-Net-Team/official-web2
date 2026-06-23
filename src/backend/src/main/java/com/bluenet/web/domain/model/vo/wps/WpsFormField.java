package com.bluenet.web.domain.model.vo.wps;

import java.util.List;

/**
 * WPS 表单字段映射常量。
 * <p>
 * 定义 WPS 表单题目标题与系统字段的映射关系，
 * 属于 WPS 表单协议 / 解析逻辑的领域知识。
 * </p>
 */
public final class WpsFormField {

    private WpsFormField() {
    }

    /** 学号 */
    public static final String STUDENT_ID = "学号";
    /** 姓名 */
    public static final String USERNAME = "姓名";
    /** 邮箱 */
    public static final String EMAIL = "邮箱";
    /** 方向 */
    public static final String DIRECTION = "方向";
    /** 专业 */
    public static final String MAJOR = "专业";
    /** 学院 */
    public static final String COLLEGE = "学院";
    /** 性别 */
    public static final String GENDER = "性别";

    /**
     * 所有需要匹配的字段列表，用于从 WPS answerContents 中提取数据。
     */
    public static final List<String> ALL = List.of(
            STUDENT_ID, USERNAME, EMAIL, DIRECTION, MAJOR, COLLEGE, GENDER);
}
