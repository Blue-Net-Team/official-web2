package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.venue.CreateVenueRequestDTO;
import com.bluenet.web.api.dto.venue.UpdateVenueRequestDTO;
import com.bluenet.web.api.dto.venue.VenueDTO;

import java.util.List;

/**
 * 场地应用服务接口
 * <p>
 * 提供场地相关的应用层服务
 * </p>
 */
public interface VenueService {
    /**
     * 获取所有场地列表
     *
     * @return 场地列表
     */
    List<VenueDTO> getVenueList();

    /**
     * 获取场地详情
     *
     * @param id
     *            场地ID
     * @return 场地详情
     */
    VenueDTO getVenueDetail(Long id);

    /**
     * 创建场地
     *
     * @param request
     *            创建请求
     * @return 创建后的场地
     */
    VenueDTO createVenue(CreateVenueRequestDTO request);

    /**
     * 更新场地
     *
     * @param id
     *            场地ID
     * @param request
     *            更新请求
     * @return 更新后的场地
     */
    VenueDTO updateVenue(Long id, UpdateVenueRequestDTO request);

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
