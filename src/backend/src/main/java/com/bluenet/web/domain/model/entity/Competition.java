package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import lombok.Data;

@Data
@TableName("tb_competition")
public class Competition {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String shortName;
    private Long logoFileId;
    private Long coverFileId;
    private String summary;
    private AwardLevel level = AwardLevel.PROVINCIAL;
    private String month;
    private String organizer;
    private Integer sortOrder;
}
