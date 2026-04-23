package com.bluenet.web.application.command.qrcode;

/**
 * 二维码聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class QrcodeCommands {

    /** 禁止实例化。 */
    private QrcodeCommands() {
    }

    /**
     * 创建咨询群二维码命令。
     * <p>
     * 用于创建新的咨询群二维码。
     * </p>
     */
    public record CreateConsultationQrcodeCommand(
            /** 文件ID */
            Long fileId) {
    }

    /**
     * 删除咨询群二维码命令。
     * <p>
     * 用于删除指定的咨询群二维码。
     * </p>
     */
    public record DeleteConsultationQrcodeCommand(
            /** ID */
            Long id) {
    }
}
