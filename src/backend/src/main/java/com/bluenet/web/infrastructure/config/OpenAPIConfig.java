package com.bluenet.web.infrastructure.config;

import com.bluenet.web.api.dto.ResponseMessage;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置：API 元信息、Bearer JWT 安全方案、全局 500 响应。 需认证的接口在 Controller
 * 方法上通过 @SecurityRequirement(name = "bearer-jwt") 声明。
 */
@Configuration
public class OpenAPIConfig {

    private static final String BEARER_JWT = "bearer-jwt";

    /** 从 ResponseMessage 类解析出的 schema，与 dto 定义保持一致。 */
    private static final Schema<?> RESPONSE_MESSAGE_SCHEMA = resolveSchema(ResponseMessage.class);

    private static final String EXAMPLE_500 = "{\"code\":500,\"msg\":\"服务器内部错误\",\"data\":null}";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(
                        new Info().title("Bluenet 后端 API")
                                .version("0.1.0")
                                .description(
                                        "Bluenet 官网后端 REST API 文档。需认证接口请在 Swagger UI 中点击 Authorize 填写 Bearer <token>。"))
                .components(
                        new Components().addSecuritySchemes(
                                BEARER_JWT,
                                new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("登录后获得的 JWT，填入时请保留 Bearer 前缀")));
    }

    /** 为所有接口在文档中统一添加 500 响应（schema 来自 ResponseMessage 类），无需在每个接口上单独声明。 */
    @Bean
    public OperationCustomizer global500ResponseCustomizer() {
        return (operation, handlerMethod) -> {
            MediaType mediaType = new MediaType().schema(RESPONSE_MESSAGE_SCHEMA).example(EXAMPLE_500);
            ApiResponse response500 = new ApiResponse().description("服务器内部错误，body 为 ResponseMessage，code=500")
                    .content(new Content().addMediaType("application/json", mediaType));
            operation.getResponses().addApiResponse("500", response500);
            return operation;
        };
    }

    private static Schema<?> resolveSchema(Class<?> clazz) {
        ResolvedSchema resolved = ModelConverters.getInstance().resolveAsResolvedSchema(new AnnotatedType(clazz));
        return resolved.schema;
    }
}
