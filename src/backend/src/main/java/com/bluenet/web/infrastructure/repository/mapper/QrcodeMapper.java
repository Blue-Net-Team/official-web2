package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.Qrcode;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QrcodeMapper extends BaseMapper<Qrcode> {
    Qrcode selectByFileId(Long fileId);
}
