package com.bluenet.web.api.dto.introduce;

import com.bluenet.web.api.dto.ResponseMessage;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 介绍图片列表响应消息
 */
@Schema(description = "介绍图片列表响应")
public class ResponseMessageIntroduceImageList extends ResponseMessage<java.util.List<IntroduceImageDTO>> {
}
