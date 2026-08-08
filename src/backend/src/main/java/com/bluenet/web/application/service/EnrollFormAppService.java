package com.bluenet.web.application.service;

import com.bluenet.web.application.command.enrollform.EnrollFormCommands;
import com.bluenet.web.application.result.enrollform.EnrollFormResult;

import java.util.Optional;

/**
 * 报名表应用服务。
 * <p>
 * 编排报名表的查询、设置/更新与删除用例。 当前报名表由 tb_file 中 type='enroll-form' 且 status='active'
 * 的最新一条记录表达。
 * </p>
 */
public interface EnrollFormAppService {

    /**
     * 查询当前报名表。
     *
     * @return 当前报名表结果；不存在时为空
     */
    Optional<EnrollFormResult> getCurrentEnrollForm();

    /**
     * 设置或更新报名表。
     * <p>
     * 先校验新文件有效性（存在、类型为 ENROLL_FORM、状态为 ACTIVE、扩展名为 pdf/doc/docx），
     * 校验全部通过后删除旧报名表文件（数据库记录与对象存储一并删除）。
     * </p>
     *
     * @param command
     *            设置报名表命令
     */
    void setEnrollForm(EnrollFormCommands.SetEnrollFormCommand command);

    /**
     * 删除当前报名表（数据库记录与对象存储一并删除）。
     */
    void deleteEnrollForm();
}
