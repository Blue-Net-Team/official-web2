package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.QrcodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class QrcodeVO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 关联文件记录标识。
     */
    private Long fileId;
    /**
     * 关联文件的领域视图对象。
     */
    private FileVO file;
    /**
     * 业务分类或枚举类型。
     */
    private QrcodeType type;
}
