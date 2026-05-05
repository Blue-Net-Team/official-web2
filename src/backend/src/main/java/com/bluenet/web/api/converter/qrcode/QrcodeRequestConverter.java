package com.bluenet.web.api.converter.qrcode;

import com.bluenet.web.application.command.qrcode.QrcodeCommands;
import com.bluenet.web.api.dto.qrcode.CreateAssessmentQrcodeRequestDTO;
import com.bluenet.web.api.dto.qrcode.UpdateAssessmentQrcodeRequestDTO;
import com.bluenet.web.api.dto.qrcode.UpdateConsultationQrcodeRequestDTO;
import org.springframework.stereotype.Component;

/**
 * 二维码请求转换器
 * <p>
 * 负责将 API 层的请求参数转换为应用层的 Command
 */
@Component
public class QrcodeRequestConverter {

    /**
     * 将创建请求参数转换为命令
     */
    public QrcodeCommands.CreateConsultationQrcodeCommand toCreateCommand(Long fileId) {
        return new QrcodeCommands.CreateConsultationQrcodeCommand(fileId);
    }

    /**
     * 将更新请求参数转换为命令
     */
    public QrcodeCommands.UpdateConsultationQrcodeCommand toUpdateCommand(Long id,
            UpdateConsultationQrcodeRequestDTO request) {
        return new QrcodeCommands.UpdateConsultationQrcodeCommand(id, request.getFileId());
    }

    /**
     * 将删除请求参数转换为命令
     */
    public QrcodeCommands.DeleteConsultationQrcodeCommand toDeleteCommand(Long id) {
        return new QrcodeCommands.DeleteConsultationQrcodeCommand(id);
    }

    /**
     * 将创建考核群二维码请求转换为命令
     */
    public QrcodeCommands.CreateAssessmentQrcodeCommand toCreateAssessmentCommand(
            CreateAssessmentQrcodeRequestDTO request) {
        return new QrcodeCommands.CreateAssessmentQrcodeCommand(request.getFileId(),
                request.getDirection(), request.getEpoch(), request.getIsShared());
    }

    /**
     * 将更新考核群二维码请求转换为命令
     */
    public QrcodeCommands.UpdateAssessmentQrcodeCommand toUpdateAssessmentCommand(Long id,
            UpdateAssessmentQrcodeRequestDTO request) {
        return new QrcodeCommands.UpdateAssessmentQrcodeCommand(id, request.getFileId(),
                request.getDirection(), request.getEpoch(), request.getIsShared());
    }

    /**
     * 将删除考核群二维码请求转换为命令
     */
    public QrcodeCommands.DeleteAssessmentQrcodeCommand toDeleteAssessmentCommand(Long id) {
        return new QrcodeCommands.DeleteAssessmentQrcodeCommand(id);
    }
}
