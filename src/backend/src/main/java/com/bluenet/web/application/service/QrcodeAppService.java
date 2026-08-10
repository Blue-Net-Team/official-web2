package com.bluenet.web.application.service;

import com.bluenet.web.application.result.qrcode.QrcodeResult;
import com.bluenet.web.application.command.qrcode.QrcodeCommands;

import java.util.List;

/**
 * 二维码应用服务接口
 * <p>
 * 定义二维码聚合在应用层的所有业务操作
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
     * 更新咨询群二维码
     *
     * @param command
     *            更新命令
     */
    void updateConsultationQrcode(QrcodeCommands.UpdateConsultationQrcodeCommand command);

    /**
     * 删除咨询群二维码
     *
     * @param command
     *            删除命令
     */
    void deleteConsultationQrcode(QrcodeCommands.DeleteConsultationQrcodeCommand command);

    /**
     * 获取考核群二维码列表
     *
     * @param direction
     *            方向（可选）
     * @param epoch
     *            考核轮次（可选）
     * @return 考核群二维码结果列表
     */
    List<QrcodeResult> getAssessmentQrcodes(String direction, Integer epoch);

    /**
     * 创建考核群二维码
     *
     * @param command
     *            创建命令
     */
    void createAssessmentQrcode(QrcodeCommands.CreateAssessmentQrcodeCommand command);

    /**
     * 更新考核群二维码
     *
     * @param command
     *            更新命令
     */
    void updateAssessmentQrcode(QrcodeCommands.UpdateAssessmentQrcodeCommand command);

    /**
     * 删除考核群二维码
     *
     * @param command
     *            删除命令
     */
    void deleteAssessmentQrcode(QrcodeCommands.DeleteAssessmentQrcodeCommand command);
}
