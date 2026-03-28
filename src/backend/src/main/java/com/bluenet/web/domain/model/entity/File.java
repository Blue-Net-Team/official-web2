package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@TableName("tb_file")
@AllArgsConstructor
@Builder
public class File {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private FileType type;
    private String url;
}
