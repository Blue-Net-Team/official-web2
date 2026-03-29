package com.bluenet.web.application.service;

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
     * 根据类型获取介绍图片列表
     *
     * @param type
     *            图片类型（必填）
     * @return 介绍图片DTO列表
     */
    List<IntroduceImageDTO> getIntroduceImages(ImageType type);
}
