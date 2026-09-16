package com.bluenet.web.api.dto.aitrace;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "AI 会话详情")
public class AiConversationDetailDTO {
    @Schema(description = "会话标识")
    private String id;
    @Schema(description = "会话创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "会话最后活跃时间")
    private LocalDateTime lastActiveAt;
    @Schema(description = "消息数：该会话下的用户提问数量")
    private Long messageCount;
    @Schema(description = "按序号排列的全部提问轨迹")
    private List<AiTurnDetailDTO> turns;
}
