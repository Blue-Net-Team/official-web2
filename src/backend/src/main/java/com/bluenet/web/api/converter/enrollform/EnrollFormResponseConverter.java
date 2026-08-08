package com.bluenet.web.api.converter.enrollform;

import com.bluenet.web.api.dto.enrollform.EnrollFormDTO;
import com.bluenet.web.application.result.enrollform.EnrollFormResult;
import org.springframework.stereotype.Component;

/**
 * 报名表响应转换器
 * <p>
 * 负责将应用层的 Result 转换为 API 层的 DTO
 * </p>
 */
@Component
public class EnrollFormResponseConverter {

    /**
     * 将 EnrollFormResult 转换为 EnrollFormDTO
     */
    public EnrollFormDTO toDTO(EnrollFormResult result) {
        return EnrollFormDTO.builder()
                .fileId(result.fileId())
                .createdAt(result.createdAt())
                .build();
    }
}
