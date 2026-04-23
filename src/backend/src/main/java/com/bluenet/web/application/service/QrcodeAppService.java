package com.bluenet.web.application.service;

import com.bluenet.web.application.QrcodeResult;
import com.bluenet.web.application.command.qrcode.QrcodeCommands;

import java.util.List;

/**
 * 二维码应用服务接口。
 * <p>
 * 定义了二维码聚合在应用层的所有业务操作。
 * </p>
 */
public interface QrcodeAppService {

    /**
     * 获取咨询群二维码列表
     *
     * @return 咨询群二维码结果列表
     */
    List<QrcodeResult> getConsultationQrcodes();

    /**
     * 创建咨询群二维码记录（通过已上传的 fileId）
     *
     * @param command
     *            创建命令
     */
    void createConsultationQrcode(QrcodeCommands.CreateConsultationQrcodeCommand command);

    /**
     * 删除咨询群二维码
     *
     * @param command
     *            删除命令
     */
    void deleteConsultationQrcode(QrcodeCommands.DeleteConsultationQrcodeCommand command);
}
