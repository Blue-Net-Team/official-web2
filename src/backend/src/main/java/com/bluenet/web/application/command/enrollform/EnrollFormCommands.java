package com.bluenet.web.application.command.enrollform;

/**
 * 报名表聚合的命令对象集合。
 * <p>
 * 定义了报名表相关应用层操作所需的命令参数。
 * </p>
 */
public class EnrollFormCommands {

    /** 禁止实例化。 */
    private EnrollFormCommands() {
    }

    /**
     * 设置或更新报名表命令。
     * <p>
     * 将指定的已确认上传文件设为当前报名表，并替换旧报名表。
     * </p>
     */
    public record SetEnrollFormCommand(
            /** 文件ID */
            Long fileId) {
    }
}
