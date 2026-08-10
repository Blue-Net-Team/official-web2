package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

@Getter
public enum FileType implements ValueEnum {
    AVATAR("avatar", "头像"),
    NORMAL_IMG("normal-img", "普通图片"),
    ASSESSMENT_ATTACHMENT("assessment-attachment", "考题附件"),
    WORK("work", "考生作品"),
    QRCODE("qrcode", "二维码"),
    KNOWLEDGE("knowledge", "知识库文档"),
    ENROLL_FORM("enroll-form", "报名表");

    private final String value;
    private final String description;

    FileType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
