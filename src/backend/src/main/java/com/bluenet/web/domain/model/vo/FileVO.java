package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class FileVO {
    private Long id;
    private String name;
    private FileType type;
    private String url;
}
