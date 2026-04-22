package com.bluenet.web.api.controller.v1.health;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.health.HealthStatusDTO;
import com.bluenet.web.api.dto.health.HealthStatusDTO.ComponentHealth;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 健康检查控制器。
 * <p>
 * 提供系统健康状态检查接口，检查后端及依赖中间件（数据库、Redis、MinIO）的健康状态。
 * </p>
 */
@Tag(name = "健康检查", description = "系统健康状态检查接口，公开访问")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HealthController {

    private final HealthEndpoint healthEndpoint;

    @Operation(summary = "健康检查", description = "检查后端及依赖中间件（数据库、Redis、MinIO）的健康状态")
    @ApiResponse(responseCode = "200", description = "成功，返回健康状态", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    @ApiResponse(responseCode = "503", description = "服务不可用，至少一个组件状态为 DOWN")
    @RequiresPermission(value = "system:health", name = "健康检查", access = AccessLevel.PUBLIC)
    @GetMapping("/health")
    public ResponseMessage<HealthStatusDTO> health() {
        HealthComponent healthComponent = healthEndpoint.health();

        Map<String, ComponentHealth> components = new LinkedHashMap<>();

        if (healthComponent instanceof CompositeHealth compositeHealth) {
            Map<String, HealthComponent> healthComponents = compositeHealth.getComponents();
            if (healthComponents != null) {
                healthComponents.forEach((key, component) -> {
                    ComponentHealth.ComponentHealthBuilder builder = ComponentHealth.builder()
                            .status(component.getStatus().getCode());

                    if (component instanceof Health componentHealth) {
                        Map<String, Object> details = componentHealth.getDetails();
                        if (details != null && details.containsKey("error")) {
                            builder.error(String.valueOf(details.get("error")));
                        }
                    }

                    components.put(key, builder.build());
                });
            }
        }

        Status status = healthComponent.getStatus();
        HealthStatusDTO dto = HealthStatusDTO.builder()
                .status(status.getCode())
                .components(components)
                .build();

        HttpStatus httpStatus = Status.DOWN.equals(status)
                ? HttpStatus.SERVICE_UNAVAILABLE
                : HttpStatus.OK;
        String msg = Status.DOWN.equals(status) ? "DOWN" : "UP";

        return new ResponseMessage<HealthStatusDTO>(httpStatus.value(), msg, dto);
    }
}
