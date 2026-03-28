package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.domain.model.vo.IntroduceImageVO;

import java.util.List;

/**
 * 介绍图片领域服务接口
 * <p>
 * 提供介绍图片相关的业务逻辑操作
 * </p>
 */
public interface IntroduceImageDomainService {
    /**
     * 获取介绍图片列表
     *
     * @param type
     *            图片类型
     * @param direction
     *            方向
     * @return 介绍图片列表
     */
    List<IntroduceImageVO> getIntroduceImages(ImageType type, Direction direction);

    /**
     * 获取竞赛相关图片列表
     *
     * @param competitionId
     *            竞赛ID
     * @return 竞赛图片列表
     */
    List<IntroduceImageVO> getCompetitionImages(Long competitionId);

    /**
     * 统计竞赛图片数量
     *
     * @param competitionId
     *            竞赛ID
     * @return 图片数量
     */
    int countCompetitionImages(Long competitionId);

    /**
     * 添加竞赛图片
     *
     * @param competitionId
     *            竞赛ID
     * @param fileId
     *            文件ID
     * @param description
     *            图片描述
     * @return 添加的图片ID
     */
    Long addCompetitionImage(Long competitionId, Long fileId, String description);

    /**
     * 删除竞赛图片
     *
     * @param imageId
     *            图片ID
     */
    void removeCompetitionImage(Long imageId);

    /**
     * 检查图片是否存在
     *
     * @param imageId
     *            图片ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(Long imageId);

    /**
     * 添加介绍图片
     *
     * @param type
     *            图片类型
     * @param fileId
     *            文件ID
     * @param direction
     *            方向（仅在 type=DIRECTION 时有效）
     * @param description
     *            图片描述
     * @return 添加的图片ID
     */
    Long addIntroduceImage(ImageType type, Long fileId, Direction direction, String description);
}
