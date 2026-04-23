package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.qrcode.ConsultationQrcodeDTO;
import com.bluenet.web.application.QrcodeResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 二维码应用层转换器
 * <p>
 * 负责应用层 Result 与 API 层 DTO 之间的转换
 * </p>
 */
@Component
public class QrcodeAppConverter {

    /**
     * 将应用层结果转换为 API 响应 DTO
     */
    public ConsultationQrcodeDTO toDTO(QrcodeResult result) {
        return ConsultationQrcodeDTO.builder()
                .id(result.id())
                .fileId(result.fileId())
                .build();
    }

    /**
     * 将应用层结果列表转换为 API 响应 DTO 列表
     */
    public List<ConsultationQrcodeDTO> toDTOList(List<QrcodeResult> results) {
        return results.stream()
                .map(this::toDTO)
                .toList();
    }
}
