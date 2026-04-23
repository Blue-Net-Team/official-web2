package com.bluenet.web.api.controller.v1;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.venue.VenueDTO;
import com.bluenet.web.application.VenueResult;
import com.bluenet.web.application.converter.VenueAppConverter;
import com.bluenet.web.application.service.VenueAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 场地公开接口
 * <p>
 * 提供场地列表查询功能，无需认证
 * </p>
 */
@Tag(name = "场地", description = "场地相关接口，公开访问")
@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
public class VenueController {
    private final VenueAppService venueAppService;
    private final VenueAppConverter venueAppConverter;

    @Operation(summary = "获取场地列表", description = "获取所有场地列表，按排序权重降序排列")
    @RequiresPermission(name = "获取场地列表", value = "venue:list", access = AccessLevel.PUBLIC)
    @GetMapping
    public ResponseMessage<List<VenueDTO>> getVenueList() {
        List<VenueResult> results = venueAppService.getAllVenues();
        return ResponseMessage.success(venueAppConverter.toDTOList(results));
    }

    @Operation(summary = "获取场地详情", description = "根据ID获取场地详情")
    @RequiresPermission(name = "获取场地详情", value = "venue:detail", access = AccessLevel.PUBLIC)
    @GetMapping("/{id}")
    public ResponseMessage<VenueDTO> getVenueById(@PathVariable Long id) {
        VenueResult result = venueAppService.getVenueDetail(id);
        return ResponseMessage.success(venueAppConverter.toDTO(result));
    }
}
