package com.bluenet.web.api.dto.college;

import com.bluenet.web.api.dto.ResponseMessage;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ResponseMessage包装类，用于Swagger文档生成
 */
@Schema(description = "学院响应")
public class ResponseMessageCollege extends ResponseMessage<CollegeDTO> {
}
