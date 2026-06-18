package com.bluenet.web.application.service;

import com.bluenet.web.application.SoftwareResourceResult;
import com.bluenet.web.application.command.softwareresource.SoftwareResourceCommands;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 软件资源应用服务接口。
 */
public interface SoftwareResourceAppService {

    /**
     * 分页查询已启用的软件资源，支持关键字搜索。
     *
     * @param direction
     *            方向；为 null 时查询所有方向。
     * @param keyword
     *            搜索关键词；为 null 或空时忽略。
     * @param pageable
     *            分页参数。
     * @return 分页的软件资源结果。
     */
    Page<SoftwareResourceResult> listActiveResources(SoftwareResourceDirection direction, String keyword,
            Pageable pageable);

    /**
     * 分页查询所有软件资源（管理后台）。
     *
     * @param pageable
     *            分页参数。
     * @return 分页的软件资源结果。
     */
    Page<SoftwareResourceResult> listAllForAdmin(Pageable pageable);

    /**
     * 创建软件资源。
     *
     * @param command
     *            创建命令。
     * @return 创建后的软件资源结果。
     */
    SoftwareResourceResult createSoftwareResource(SoftwareResourceCommands.CreateSoftwareResourceCommand command);

    /**
     * 更新软件资源。
     *
     * @param command
     *            更新命令。
     * @return 更新后的软件资源结果。
     */
    SoftwareResourceResult updateSoftwareResource(SoftwareResourceCommands.UpdateSoftwareResourceCommand command);

    /**
     * 删除软件资源。
     *
     * @param id
     *            资源 ID。
     */
    void deleteSoftwareResource(Long id);
}
