package com.bluenet.web.application.service;

import com.bluenet.web.application.result.venue.VenueResult;
import com.bluenet.web.application.command.venue.VenueCommands;

import java.util.List;

/**
 * 场地应用服务接口。
 * <p>
 * 定义了场地聚合在应用层的所有业务操作。
 * </p>
 */
public interface VenueAppService {

    /**
     * 获取所有场地列表
     *
     * @return 场地结果列表
     */
    List<VenueResult> getAllVenues();

    /**
     * 获取场地详情
     *
     * @param id
     *            场地ID
     * @return 场地结果
     */
    VenueResult getVenueDetail(Long id);

    /**
     * 创建场地
     *
     * @param command
     *            创建命令
     * @return 创建后的场地结果
     */
    VenueResult createVenue(VenueCommands.CreateVenueCommand command);

    /**
     * 更新场地
     *
     * @param command
     *            更新命令
     * @return 更新后的场地结果
     */
    VenueResult updateVenue(VenueCommands.UpdateVenueCommand command);

    /**
     * 删除场地
     *
     * @param id
     *            场地ID
     */
    void deleteVenue(Long id);

    /**
     * 更新场地图片
     *
     * @param id
     *            场地ID
     * @param imageFileId
     *            图片文件ID
     */
    void updateVenueImage(Long id, Long imageFileId);
}
