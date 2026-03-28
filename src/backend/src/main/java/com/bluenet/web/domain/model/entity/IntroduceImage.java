package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ImageType;
import lombok.Data;

@Data
@TableName("tb_introduce_image")
public class IntroduceImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private ImageType type;
    private String description;
    private Long fileId;
    private Direction direction;
    private Long competitionId;
    private Integer sortOrder;
}
