package com.bluenet.web.application.command.wpsform;

import com.bluenet.web.domain.model.enumerate.Direction;
import lombok.Builder;

import java.util.List;

/**
 * WPS 表单处理命令对象集合。
 */
public class WpsFormCommands {

    private WpsFormCommands() {
    }

    /**
     * WPS 表单字段映射规则，通过题目标题匹配对应的用户字段。
     */
    public static final String FIELD_STUDENT_ID = "学号";
    public static final String FIELD_USERNAME = "姓名";
    public static final String FIELD_EMAIL = "邮箱";
    public static final String FIELD_DIRECTION = "方向";
    public static final String FIELD_MAJOR = "专业";

    /**
     * 所有需要匹配的字段列表，用于从 answerContents 中提取数据
     */
    public static final List<String> ALL_FIELDS = List.of(
            FIELD_STUDENT_ID, FIELD_USERNAME, FIELD_EMAIL, FIELD_DIRECTION, FIELD_MAJOR);

    /**
     * 通过 WPS 表单创建用户的命令
     */
    @Builder
    public record CreateUserFromWpsCommand(
            String studentId,
            String username,
            String email,
            Direction direction,
            String major) {
    }
}
