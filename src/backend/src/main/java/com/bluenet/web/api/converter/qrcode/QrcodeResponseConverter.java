package com.bluenet.web.api.converter.qrcode;

import com.bluenet.web.api.dto.qrcode.AssessmentQrcodeDTO;
import com.bluenet.web.api.dto.qrcode.ConsultationQrcodeDTO;
import com.bluenet.web.application.result.qrcode.QrcodeResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 二维码响应转换器
 * <p>
 * 负责将应用层的 Result 转换为 API 层的 DTO
 */
@Component
public class QrcodeResponseConverter {

    /**
     * 将 QrcodeResult 转换为 ConsultationQrcodeDTO
     */
    public ConsultationQrcodeDTO toConsultationDTO(QrcodeResult result) {
        return ConsultationQrcodeDTO.builder()
                .id(result.id())
                .fileId(result.fileId())
                .build();
    }

    /**
     * 将 QrcodeResult 列表转换为 ConsultationQrcodeDTO 列表
     */
    public List<ConsultationQrcodeDTO> toConsultationDTOList(List<QrcodeResult> results) {
        return results.stream()
                .map(this::toConsultationDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将 QrcodeResult 转换为 AssessmentQrcodeDTO
     */
    public AssessmentQrcodeDTO toAssessmentDTO(QrcodeResult result) {
        return AssessmentQrcodeDTO.builder()
                .id(result.id())
                .fileId(result.fileId())
                .direction(result.direction())
                .epoch(result.epoch())
                .isShared(result.isShared())
                .build();
    }

    /**
     * 将 QrcodeResult 列表转换为 AssessmentQrcodeDTO 列表
     */
    public List<AssessmentQrcodeDTO> toAssessmentDTOList(List<QrcodeResult> results) {
        return results.stream()
                .map(this::toAssessmentDTO)
                .collect(Collectors.toList());
    }
}
