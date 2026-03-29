package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.domain.model.vo.IntroduceImageVO;

import java.util.List;

/**
 * 介绍图片仓库接口
 * <p>
 * 负责介绍图片数据的持久化操作
 * </p>
 */
public interface IntroduceImageRepository {
    /**
     * 根据类型查询介绍图片
     *
     * @param type
     *            图片类型
     * @return 介绍图片列表
     */
    List<IntroduceImageVO> findByType(ImageType type);

    /**
     * 根据类型和竞赛ID查询介绍图片
     *
     * @param type
     *            图片类型
     * @param competitionId
     *            竞赛ID
     * @return 介绍图片列表
     */
    List<IntroduceImageVO> findByTypeAndCompetitionId(ImageType type, Long competitionId);

    /**
     * 统计指定竞赛的图片数量
     *
     * @param type
     *            图片类型
     * @param competitionId
     *            竞赛ID
     * @return 图片数量
     */
    int countByTypeAndCompetitionId(ImageType type, Long competitionId);

    /**
     * 保存介绍图片
     *
     * @param introduceImage
     *            介绍图片实体
     * @return 保存后的图片ID
     */
    Long save(com.bluenet.web.domain.model.entity.IntroduceImage introduceImage);

    /**
     * 根据ID删除介绍图片
     *
     * @param id
     *            图片ID
     */
    void deleteById(Long id);

    /**
     * 检查介绍图片是否存在
     *
     * @param id
     *            图片ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(Long id);
}
