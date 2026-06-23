package com.bluenet.web.api.dto.wps;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WPS 智能表单数据推送回调请求体。
 * <p>
 * 当 WPS 表单配置了数据推送后，表单提交/修改等事件会 POST JSON 到此接口。
 * 字段名由 WPS 平台定义，不可更改。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WpsCallbackRequestDTO {

    /**
     * 答卷 ID
     */
    private String rid;

    /**
     * 表单 ID
     */
    private String formId;

    /**
     * 表单标题
     */
    private String formTitle;

    /**
     * 应用 ID
     */
    private String aid;

    /**
     * 事件时间戳
     */
    private Long eventTs;

    /**
     * 提交用户 ID
     */
    private String creatorId;

    /**
     * 提交用户昵称
     */
    private String creatorName;

    /**
     * 事件类型：create_answer / update_answer / delete_answer / bind
     */
    @NotBlank(message = "event 不能为空")
    @Pattern(regexp = "^(bind|create_answer|update_answer|delete_answer)$", message = "event 必须为 bind、create_answer、update_answer 或 delete_answer")
    private String event;

    /**
     * 答卷内容列表，每项对应一个表单题目
     */
    @Valid
    @NotEmpty(message = "answerContents 不能为空", groups = {CreateAnswerValidation.class})
    private List<@Valid AnswerContent> answerContents;

    /**
     * create_answer 事件专用验证组
     */
    public interface CreateAnswerValidation {}

    /**
     * 单个题目的回答内容
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerContent {

        /**
         * 题目 ID（WPS 自动生成）
         */
        private String qid;

        /**
         * 题目类型
         */
        private String type;

        /**
         * 题目标题（中文，如"学号"、"姓名"）
         */
        @NotBlank(message = "题目 title 不能为空")
        private String title;

        /**
         * 填写值（WPS 可能返回字符串或数组，如多选返回 ["选项1","选项2"]）
         */
        private Object value;
    }
}
