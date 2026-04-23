package com.bluenet.web.api.controller.v1.member;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.experience.ExperienceDTO;
import com.bluenet.web.api.dto.member.DirectionLeaderDTO;
import com.bluenet.web.api.dto.member.MemberBriefDTO;
import com.bluenet.web.api.dto.member.MemberDetailDTO;
import com.bluenet.web.api.dto.member.MemberListQueryDTO;
import com.bluenet.web.api.converter.member.MemberRequestConverter;
import com.bluenet.web.application.MemberResult;
import com.bluenet.web.application.command.member.MemberCommands;
import com.bluenet.web.application.converter.MemberAppConverter;
import com.bluenet.web.application.service.MemberAppService;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "团队成员", description = "公开的团队成员信息接口")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberAppService memberAppService;
    private final MemberRequestConverter memberRequestConverter;
    private final MemberAppConverter memberAppConverter;

    @Operation(summary = "获取团队成员列表", description = "分页查询团队成员列表，支持按方向筛选，按入学年份降序排列（新人在前）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功返回成员列表"),
    })
    @RequiresPermission(name = "获取团队成员列表", value = "member:list", access = AccessLevel.PUBLIC)
    @GetMapping
    public ResponseMessage<PageDTO<MemberBriefDTO>> getMemberList(
            @Parameter(description = "页码，从0开始") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页数量，默认20，最大100") @RequestParam(required = false) Integer size,
            @Parameter(description = "方向筛选") @RequestParam(required = false) Direction direction) {
        MemberListQueryDTO query = MemberListQueryDTO.builder()
                .page(page)
                .size(size)
                .direction(direction)
                .build();
        MemberCommands.GetMemberListCommand command = memberRequestConverter.toCommand(query);
        Page<MemberResult> resultPage = memberAppService.getMemberList(command);
        Page<MemberBriefDTO> dtoPage = resultPage.map(memberAppConverter::toBriefDTO);
        return ResponseMessage.success(PageDTO.from(dtoPage));
    }

    @Operation(summary = "获取成员详情", description = "获取指定成员的详细信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功返回成员详情"),
            @ApiResponse(responseCode = "404", description = "成员不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "获取成员详情", value = "member:detail", access = AccessLevel.PUBLIC)
    @GetMapping("/{id}")
    public ResponseMessage<MemberDetailDTO> getMemberById(
            @Parameter(description = "成员ID") @PathVariable Long id) {
        try {
            MemberResult result = memberAppService.getMemberById(id);
            return ResponseMessage.success(memberAppConverter.toDetailDTO(result));
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }

    @Operation(summary = "获取方向负责人", description = "获取各方向的负责人信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功返回方向负责人列表"),
    })
    @RequiresPermission(name = "获取方向负责人", value = "member:direction-leaders", access = AccessLevel.PUBLIC)
    @GetMapping("/direction-leaders")
    public ResponseMessage<List<DirectionLeaderDTO>> getDirectionLeaders() {
        List<MemberResult> results = memberAppService.getDirectionLeaders();
        return ResponseMessage.success(memberAppConverter.toDirectionLeaderDTOs(results));
    }

    @Operation(summary = "获取成员经历", description = "获取指定团队成员的经历列表（公开接口，无需登录）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功返回成员经历列表"),
            @ApiResponse(responseCode = "404", description = "成员不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "查看成员经历", value = "member:experience:view", access = AccessLevel.PUBLIC)
    @GetMapping("/{memberId}/experiences")
    public ResponseMessage<List<ExperienceDTO>> getMemberExperiences(
            @Parameter(description = "成员ID") @PathVariable Long memberId,
            @Parameter(description = "经历类型：PROJECT/COMPETITION/INTERNSHIP") @RequestParam(required = false) String type) {
        try {
            return ResponseMessage.success(memberAppService.getMemberExperiences(memberId, type));
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }
}
