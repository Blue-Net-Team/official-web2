package com.bluenet.web.application.service;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.api.dto.introduce.IntroduceImageDTO;

import java.util.List;

/**
 * 介绍图片应用服务接口
 * <p>
 * 提供介绍图片相关的应用层业务逻辑，负责VO到DTO的转换。
 * </p>
 */
public interface IntroduceImageService {
    /**
     * 根据类型和方向获取介绍图片列表
     *
     * @param type
     *            图片类型（必填）
     * @param direction
     *            方向（可选，仅在 type=DIRECTION 时有效）
     * @return 介绍图片DTO列表
     * @throws IllegalArgumentException
     *             当参数验证失败时抛出
     */
    List<IntroduceImageDTO> getIntroduceImages(ImageType type, Direction direction);
}
