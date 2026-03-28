package com.bluenet.web.application.service;

import org.springframework.core.io.Resource;

/**
 * 文件下载服务接口
 * <p>
 * 提供文件下载功能，包括权限校验
 * </p>
 */
public interface FileDownloadService {

    /**
     * 下载文件
     * <p>
     * 根据文件ID下载文件，并进行权限校验：
     * <ul>
     * <li>WORK 类型：校验提交者或角色 >= MEMBER</li>
     * <li>ASSESSMENT_ATTACHMENT 类型：校验方向匹配</li>
     * <li>AVATAR 类型：根据关联表决定</li>
     * <li>NORMAL_IMG/QRCODE：公开访问</li>
     * </ul>
     * </p>
     *
     * @param fileId
     *            文件ID
     * @return 文件资源
     * @throws com.bluenet.web.domain.exception.DataNotFound
     *             文件不存在
     * @throws com.bluenet.web.domain.exception.Forbidden
     *             权限不足
     */
    Resource downloadFile(Long fileId);
}
