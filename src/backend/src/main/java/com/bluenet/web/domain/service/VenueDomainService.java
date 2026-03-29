package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.VenueVO;

import java.util.List;
import java.util.Optional;

/**
 * 场地领域服务接口
 * <p>
 * 提供场地相关的领域业务操作
 * </p>
 */
public interface VenueDomainService {
    /**
     * 获取所有场地（按排序号降序）
     *
     * @return 场地列表
     */
    List<VenueVO> getAllVenues();

    /**
     * 根据ID获取场地
     *
     * @param id
     *            场地ID
     * @return 场地信息
     */
    Optional<VenueVO> getVenueById(Long id);

    /**
     * 创建场地
     *
     * @param name
     *            场地名称
     * @param subtitle
     *            场地副标题
     * @param description
     *            场地描述
     * @param imageFileId
     *            图片文件ID
     * @param sortOrder
     *            排序权重
     * @return 创建后的场地ID
     */
    Long createVenue(String name, String subtitle, String description, Long imageFileId, Integer sortOrder);

    /**
     * 更新场地
     *
     * @param id
     *            场地ID
     * @param name
     *            场地名称
     * @param subtitle
     *            场地副标题
     * @param description
     *            场地描述
     * @param imageFileId
     *            图片文件ID
     * @param sortOrder
     *            排序权重
     */
    void updateVenue(Long id, String name, String subtitle, String description, Long imageFileId, Integer sortOrder);

    /**
     * 删除场地
     *
     * @param id
     *            场地ID
     */
    void deleteVenue(Long id);

    /**
     * 检查场地是否存在
     *
     * @param id
     *            场地ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(Long id);

    /**
     * 更新场地图片
     *
     * @param id
     *            场地ID
     * @param imageFileId
     *            图片文件ID
     */
    void updateImage(Long id, Long imageFileId);
}
