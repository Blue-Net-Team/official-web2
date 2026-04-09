package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class VerifyCodeVO {
    private String target;
    private String code;
    private LocalDateTime expireAt;
    private boolean used;
    private String scene;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireAt);
    }
}
