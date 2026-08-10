package com.bluenet.web.api.controller.v1.softwareresource;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.softwareresource.SoftwareResourceDTO;
import com.bluenet.web.api.dto.softwareresource.SoftwareResourceListRequestDTO;
import com.bluenet.web.api.converter.softwareresource.SoftwareResourceRequestConverter;
import com.bluenet.web.api.converter.softwareresource.SoftwareResourceResponseConverter;
import com.bluenet.web.application.result.softwareresource.SoftwareResourceResult;
import com.bluenet.web.application.service.SoftwareResourceAppService;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 软件资源公开接口。
 */
@Tag(name = "软件资源", description = "软件资源公开接口")
@RestController
@RequestMapping("/api/v1/software-resources")
@RequiredArgsConstructor
public class SoftwareResourceController {

    private final SoftwareResourceAppService softwareResourceAppService;
    private final SoftwareResourceRequestConverter requestConverter;
    private final SoftwareResourceResponseConverter responseConverter;

    @Operation(summary = "获取软件资源列表", description = "按方向、关键字分页查询已启用的软件资源，方向为空时查询全部")
    @RequiresPermission(value = "software-resource:list", name = "获取软件资源列表", access = AccessLevel.PUBLIC)
    @GetMapping
    public ResponseMessage<PageDTO<SoftwareResourceDTO>> listSoftwareResources(
            @Valid @ModelAttribute SoftwareResourceListRequestDTO request) {
        SoftwareResourceDirection direction = requestConverter.toDirection(request);
        String keyword = request.getKeyword();
        Pageable pageable = requestConverter.toPageable(request);
        Page<SoftwareResourceResult> resultPage = softwareResourceAppService
                .listActiveResources(direction, keyword, pageable);
        return ResponseMessage.success(responseConverter.toPageDTO(resultPage));
    }
}
