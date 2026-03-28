package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.introduce.IntroduceImageDTO;
import com.bluenet.web.application.converter.IntroduceImageConverter;
import com.bluenet.web.application.service.IntroduceImageService;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.domain.model.vo.IntroduceImageVO;
import com.bluenet.web.domain.service.IntroduceImageDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 介绍图片应用服务实现
 * <p>
 * 提供介绍图片相关的应用层业务逻辑，负责VO到DTO的转换。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class IntroduceImageServiceImpl implements IntroduceImageService {
    private final IntroduceImageDomainService introduceImageDomainService;
    private final IntroduceImageConverter introduceImageConverter;

    @Override
    public List<IntroduceImageDTO> getIntroduceImages(ImageType type, Direction direction) {
        // 参数验证：direction 仅在 type=DIRECTION 时有效
        if (direction != null && type != ImageType.DIRECTION) {
            throw new IllegalArgumentException("direction 参数仅在 type=direction 时有效");
        }

        // 调用领域服务获取VO列表
        List<IntroduceImageVO> introduceImageVOList = introduceImageDomainService.getIntroduceImages(type, direction);

        // 转换为DTO列表
        return introduceImageConverter.convertToIntroduceImageDTOList(introduceImageVOList);
    }
}
