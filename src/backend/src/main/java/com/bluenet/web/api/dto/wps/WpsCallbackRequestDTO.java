package com.bluenet.web.api.dto.wps;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * WPS 智能表单数据推送回调请求体基类。
 * <p>
 * 当 WPS 表单配置了数据推送后，表单提交/修改等事件会 POST JSON 到此接口。 Jackson 根据 {@code event}
 * 字段自动反序列化为对应的子类，避免 Controller 手动分发。
 * </p>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "event", visible = true, defaultImpl = WpsProbeCallbackRequestDTO.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = WpsBindCallbackRequestDTO.class, name = "bind"),
        @JsonSubTypes.Type(value = WpsCreateAnswerCallbackRequestDTO.class, name = "create_answer")
})
@Getter
@Setter
@NoArgsConstructor
public abstract class WpsCallbackRequestDTO {

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
     * 事件类型：create_answer / bind 等。
     *
     * @return 事件类型字符串
     */
    public abstract String getEvent();
}
