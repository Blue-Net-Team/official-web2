package com.bluenet.web.api.controller.v1.qrcode;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.qrcode.ConsultationQrcodeDTO;
import com.bluenet.web.application.QrcodeResult;
import com.bluenet.web.api.converter.qrcode.QrcodeResponseConverter;
import com.bluenet.web.application.service.QrcodeAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 二维码公开接口控制器
 * <p>
 * 提供二维码相关的公开接口，无需登录即可访问
 * </p>
 */
@Tag(name = "二维码", description = "二维码公开接口，无需登录即可访问")
@RestController
@RequestMapping("/api/v1/qrcodes")
@RequiredArgsConstructor
public class QrcodeController {

    private final QrcodeAppService qrcodeAppService;
    private final QrcodeResponseConverter qrcodeResponseConverter;

    @Operation(summary = "获取咨询群二维码列表", description = "获取所有咨询群二维码，公开接口")
    @RequiresPermission(name = "获取咨询群列表", value = "qrcode:consultation:list", access = AccessLevel.PUBLIC)
    @GetMapping("/consultation")
    public ResponseMessage<List<ConsultationQrcodeDTO>> getConsultationQrcodes() {
        List<QrcodeResult> results = qrcodeAppService.getConsultationQrcodes();
        return ResponseMessage.success(qrcodeResponseConverter.toConsultationDTOList(results));
    }
}
