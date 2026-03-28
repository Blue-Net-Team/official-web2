package com.bluenet.web.api.dto.college;

import com.bluenet.web.api.dto.ResponseMessage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * ResponseMessage包装类，用于Swagger文档生成
 */
@Schema(description = "学院列表响应")
public class ResponseMessageCollegeList extends ResponseMessage<List<CollegeDTO>> {
}
