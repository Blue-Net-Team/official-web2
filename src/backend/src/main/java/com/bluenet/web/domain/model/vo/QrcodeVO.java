package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.QrcodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class QrcodeVO {
    private Long id;
    private Long fileId;
    private FileVO file;
    private QrcodeType type;
}
