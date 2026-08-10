package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.softwareresource.BatchSortRequestDTO;
import com.bluenet.web.api.dto.softwareresource.CreateSoftwareResourceRequestDTO;
import com.bluenet.web.api.dto.softwareresource.SoftwareResourceDTO;
import com.bluenet.web.api.dto.softwareresource.UpdateSoftwareResourceRequestDTO;
import com.bluenet.web.api.converter.softwareresource.SoftwareResourceResponseConverter;
import com.bluenet.web.application.result.softwareresource.SoftwareResourceResult;
import com.bluenet.web.application.service.SoftwareResourceAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminSoftwareResourceController 集成测试")
class AdminSoftwareResourceControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SoftwareResourceAppService softwareResourceAppService;

    @MockitoBean
    private SoftwareResourceResponseConverter softwareResourceResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private SoftwareResourceResult stubResult() {
        return new SoftwareResourceResult(
                1L,
                "PyCharm",
                SoftwareResourceDirection.COMPUTER_VISION,
                "IDE",
                "Python 集成开发环境",
                "https://example.com/pycharm",
                10,
                SoftwareResourceStatus.ACTIVE);
    }

    private SoftwareResourceDTO stubDTO() {
        return SoftwareResourceDTO.builder()
                .id(1L)
                .name("PyCharm")
                .direction(SoftwareResourceDirection.COMPUTER_VISION)
                .status(SoftwareResourceStatus.ACTIVE)
                .build();
    }

    private PageDTO<SoftwareResourceDTO> stubPageDTO() {
        return new PageDTO<>(
                List.of(stubDTO()),
                1,
                1,
                0,
                20,
                1,
                true,
                true,
                false);
    }

    @Test
    @DisplayName("listSoftwareResources: 超级管理员应返回分页软件资源列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "software-resource:admin-list" })
    void listSoftwareResources_asSuperAdmin_shouldReturnPagedResources() throws Exception {
        Page<SoftwareResourceResult> resultPage = new PageImpl<>(List.of(stubResult()));
        when(softwareResourceAppService.listAllForAdmin(any())).thenReturn(resultPage);
        when(softwareResourceResponseConverter.toPageDTO(any())).thenReturn(stubPageDTO());

        MvcResult mvcResult = mockMvc.perform(
                get("/api/v1/admin/software-resources")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("listSoftwareResources: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void listSoftwareResources_asMember_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/software-resources"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("createSoftwareResource: 超级管理员应成功创建软件资源")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "software-resource:create" })
    void createSoftwareResource_asSuperAdmin_shouldReturnCreatedResource() throws Exception {
        SoftwareResourceResult result = stubResult();
        SoftwareResourceDTO dto = stubDTO();
        when(softwareResourceAppService.createSoftwareResource(any())).thenReturn(result);
        when(softwareResourceResponseConverter.toDTO(any(SoftwareResourceResult.class))).thenReturn(dto);

        CreateSoftwareResourceRequestDTO request = CreateSoftwareResourceRequestDTO.builder()
                .name("PyCharm")
                .direction(SoftwareResourceDirection.COMPUTER_VISION)
                .category("IDE")
                .description("Python 集成开发环境")
                .externalUrl("https://example.com/pycharm")
                .sortOrder(10)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/admin/software-resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("PyCharm"))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("createSoftwareResource: 软件名称为空应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "software-resource:create" })
    void createSoftwareResource_withBlankName_shouldReturn400() throws Exception {
        CreateSoftwareResourceRequestDTO request = CreateSoftwareResourceRequestDTO.builder()
                .name("")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/admin/software-resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("updateSoftwareResource: 超级管理员应成功更新软件资源")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "software-resource:update" })
    void updateSoftwareResource_asSuperAdmin_shouldReturnUpdatedResource() throws Exception {
        SoftwareResourceResult result = stubResult();
        SoftwareResourceDTO dto = stubDTO();
        when(softwareResourceAppService.updateSoftwareResource(any())).thenReturn(result);
        when(softwareResourceResponseConverter.toDTO(any(SoftwareResourceResult.class))).thenReturn(dto);

        UpdateSoftwareResourceRequestDTO request = UpdateSoftwareResourceRequestDTO.builder()
                .name("PyCharm")
                .direction(SoftwareResourceDirection.COMPUTER_VISION)
                .category("IDE")
                .description("Python 集成开发环境")
                .externalUrl("https://example.com/pycharm")
                .sortOrder(10)
                .status(SoftwareResourceStatus.ACTIVE)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/software-resources/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateSoftwareResource: 软件资源不存在应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "software-resource:update" })
    void updateSoftwareResource_notFound_shouldReturn404() throws Exception {
        when(softwareResourceAppService.updateSoftwareResource(any())).thenThrow(new DataNotFound("软件资源不存在"));

        UpdateSoftwareResourceRequestDTO request = UpdateSoftwareResourceRequestDTO.builder()
                .name("PyCharm")
                .direction(SoftwareResourceDirection.COMPUTER_VISION)
                .externalUrl("https://example.com/pycharm")
                .status(SoftwareResourceStatus.ACTIVE)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/software-resources/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("deleteSoftwareResource: 超级管理员应成功删除软件资源")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "software-resource:delete" })
    void deleteSoftwareResource_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(softwareResourceAppService).deleteSoftwareResource(1L);

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/admin/software-resources/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("batchUpdateSortOrder: 超级管理员应成功批量调整软件资源排序")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "software-resource:sort" })
    void batchUpdateSortOrder_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(softwareResourceAppService).batchUpdateSortOrder(any());

        BatchSortRequestDTO request = BatchSortRequestDTO.builder()
                .items(
                        List.of(
                                BatchSortRequestDTO.SortItemDTO.builder().id(1L).sortOrder(1).build(),
                                BatchSortRequestDTO.SortItemDTO.builder().id(2L).sortOrder(2).build()))
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/software-resources/sort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("batchUpdateSortOrder: 排序列表为空应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "software-resource:sort" })
    void batchUpdateSortOrder_withEmptyItems_shouldReturn400() throws Exception {
        BatchSortRequestDTO request = BatchSortRequestDTO.builder()
                .items(List.of())
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/software-resources/sort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);
    }
}
