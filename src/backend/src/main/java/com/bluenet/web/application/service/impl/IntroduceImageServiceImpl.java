package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.introduce.IntroduceImageDTO;
import com.bluenet.web.application.converter.IntroduceImageConverter;
import com.bluenet.web.application.service.IntroduceImageService;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.domain.model.vo.IntroduceImageVO;
import com.bluenet.web.domain.repository.IntroduceImageRepository;
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
    private final IntroduceImageRepository introduceImageRepository;
    private final IntroduceImageConverter introduceImageConverter;

    @Override
    public List<IntroduceImageDTO> getIntroduceImages(ImageType type) {
        List<IntroduceImageVO> introduceImageVOList = introduceImageRepository.findByType(type);
        return introduceImageConverter.convertToIntroduceImageDTOList(introduceImageVOList);
    }
}
