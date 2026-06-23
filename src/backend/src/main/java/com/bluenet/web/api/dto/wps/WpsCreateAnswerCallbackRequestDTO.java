package com.bluenet.web.api.dto.wps;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * WPS 表单 create_answer 事件回调请求体。
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WpsCreateAnswerCallbackRequestDTO extends WpsCallbackRequestDTO {

    private static final String EVENT = "create_answer";

    /**
     * 答卷内容列表，每项对应一个表单题目
     */
    @Valid
    @NotEmpty(message = "answerContents 不能为空")
    private List<@Valid AnswerContent> answerContents;

    @Override
    public String getEvent() {
        return EVENT;
    }

    /**
     * 单个题目的回答内容
     */
    @Getter
    @Setter
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
