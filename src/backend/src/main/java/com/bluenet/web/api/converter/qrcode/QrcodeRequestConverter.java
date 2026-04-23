package com.bluenet.web.api.converter.qrcode;

import com.bluenet.web.application.command.qrcode.QrcodeCommands;
import org.springframework.stereotype.Component;

/**
 * 二维码请求转换器
 * <p>
 * 负责将 API 层的请求参数转换为应用层的 Command
 * </p>
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
     * 将删除请求参数转换为命令
     */
    public QrcodeCommands.DeleteConsultationQrcodeCommand toDeleteCommand(Long id) {
        return new QrcodeCommands.DeleteConsultationQrcodeCommand(id);
    }
}
