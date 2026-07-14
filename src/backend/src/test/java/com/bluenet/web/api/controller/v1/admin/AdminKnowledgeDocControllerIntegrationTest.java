package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.knowledge.KnowledgeDocRequestConverter;
import com.bluenet.web.api.converter.knowledge.KnowledgeDocResponseConverter;
import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.knowledge.KnowledgeChunkListItemResponseDTO;
import com.bluenet.web.api.dto.knowledge.KnowledgeDocDetailResponseDTO;
import com.bluenet.web.api.dto.knowledge.KnowledgeDocListItemResponseDTO;
import com.bluenet.web.api.dto.knowledge.KnowledgeTagListItemResponseDTO;
import com.bluenet.web.api.dto.knowledge.UpdateTagDescriptionRequestDTO;
import com.bluenet.web.application.result.knowledge.KnowledgeChunkResult;
import com.bluenet.web.application.result.knowledge.KnowledgeDocResult;
import com.bluenet.web.application.result.knowledge.KnowledgeTagResult;
import com.bluenet.web.application.service.KnowledgeBaseAppService;
import com.bluenet.web.application.service.KnowledgeDocQueryService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.DocParseStatus;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminKnowledgeDocController 集成测试")
class AdminKnowledgeDocControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KnowledgeBaseAppService knowledgeBaseAppService;

    @MockitoBean
    private KnowledgeDocQueryService knowledgeDocQueryService;

    @MockitoBean
    private KnowledgeDocResponseConverter responseConverter;

    @Autowired
    private KnowledgeDocRequestConverter requestConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private KnowledgeDocResult.Detail docDetailResult() {
        return new KnowledgeDocResult.Detail(
                1L,
                10L,
                "文档标题",
                DocParseStatus.PENDING,
                0,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    private KnowledgeDocDetailResponseDTO docDetailDTO() {
        return new KnowledgeDocDetailResponseDTO(
                1L,
                10L,
                "文档标题",
                DocParseStatus.PENDING,
                0,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    private PageDTO<KnowledgeDocListItemResponseDTO> docListPageDTO() {
        KnowledgeDocListItemResponseDTO item = new KnowledgeDocListItemResponseDTO(
                1L,
                10L,
                "文档标题",
                DocParseStatus.PENDING,
                0,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
        return new PageDTO<>(
                List.of(item),
                1L,
                1,
                0,
                10,
                1,
                true,
                true,
                false);
    }

    private PageDTO<KnowledgeChunkListItemResponseDTO> chunkListPageDTO() {
        KnowledgeChunkListItemResponseDTO item = new KnowledgeChunkListItemResponseDTO(
                1L,
                1L,
                "分段内容",
                List.of("tag"),
                "source");
        return new PageDTO<>(
                List.of(item),
                1L,
                1,
                0,
                10,
                1,
                true,
                true,
                false);
    }

    private PageDTO<KnowledgeTagListItemResponseDTO> tagListPageDTO() {
        KnowledgeTagListItemResponseDTO item = new KnowledgeTagListItemResponseDTO(
                1L,
                "标签",
                "标签描述",
                5);
        return new PageDTO<>(
                List.of(item),
                1L,
                1,
                0,
                10,
                1,
                true,
                true,
                false);
    }

    @Test
    @DisplayName("listDocuments: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void listDocuments_asMember_shouldReturn403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/knowledge/docs"))
                .andExpect(status().isForbidden())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("uploadDocument: 超级管理员应成功上传知识库文档")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "knowledge:doc:upload" })
    void uploadDocument_asSuperAdmin_shouldReturnDetail() throws Exception {
        when(knowledgeBaseAppService.uploadDocument(any()))
                .thenReturn(new KnowledgeDocResult.Uploaded(1L, DocParseStatus.PENDING));
        when(knowledgeDocQueryService.getDocumentDetail(1L)).thenReturn(docDetailResult());
        when(responseConverter.toDetailDTO(any(KnowledgeDocResult.Detail.class))).thenReturn(docDetailDTO());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.md",
                "text/markdown",
                "# 测试文档".getBytes());

        MvcResult result = mockMvc.perform(
                multipart("/api/v1/admin/knowledge/docs")
                        .file(file)
                        .param("title", "文档标题"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("listDocuments: 超级管理员应返回分页文档列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "knowledge:doc:list" })
    void listDocuments_asSuperAdmin_shouldReturnPagedDocs() throws Exception {
        KnowledgeDocResult.ListItem item = new KnowledgeDocResult.ListItem(
                1L,
                10L,
                "文档标题",
                DocParseStatus.PENDING,
                0,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
        when(knowledgeDocQueryService.listDocuments(any())).thenReturn(new PageImpl<>(List.of(item)));
        when(responseConverter.toDocListPageDTO(any())).thenReturn(docListPageDTO());

        MvcResult result = mockMvc.perform(
                get("/api/v1/admin/knowledge/docs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getDocumentDetail: 超级管理员应返回文档详情")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "knowledge:doc:detail" })
    void getDocumentDetail_asSuperAdmin_shouldReturnDetail() throws Exception {
        when(knowledgeDocQueryService.getDocumentDetail(1L)).thenReturn(docDetailResult());
        when(responseConverter.toDetailDTO(any(KnowledgeDocResult.Detail.class))).thenReturn(docDetailDTO());

        MvcResult result = mockMvc.perform(get("/api/v1/admin/knowledge/docs/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getDocumentDetail: 文档不存在时应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "knowledge:doc:detail" })
    void getDocumentDetail_whenNotFound_shouldReturn404() throws Exception {
        when(knowledgeDocQueryService.getDocumentDetail(1L)).thenReturn(null);

        MvcResult result = mockMvc.perform(get("/api/v1/admin/knowledge/docs/{id}", 1L))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("listChunks: 超级管理员应返回文档分段列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "knowledge:doc:chunks" })
    void listChunks_asSuperAdmin_shouldReturnPagedChunks() throws Exception {
        KnowledgeChunkResult.ListItem item = new KnowledgeChunkResult.ListItem(
                1L,
                1L,
                "分段内容",
                List.of("tag"),
                "source");
        when(knowledgeDocQueryService.listChunks(any(Long.class), any())).thenReturn(new PageImpl<>(List.of(item)));
        when(responseConverter.toChunkListPageDTO(any())).thenReturn(chunkListPageDTO());

        MvcResult result = mockMvc.perform(
                get("/api/v1/admin/knowledge/docs/{id}/chunks", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("reparseDocument: 超级管理员应成功触发重新解析")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "knowledge:doc:reparse" })
    void reparseDocument_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(knowledgeBaseAppService).reparse(any());

        MvcResult result = mockMvc.perform(post("/api/v1/admin/knowledge/docs/{id}/reparse", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("reparseDocument: 文档不存在时应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "knowledge:doc:reparse" })
    void reparseDocument_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new DataNotFound("文档不存在")).when(knowledgeBaseAppService).reparse(any());

        MvcResult result = mockMvc.perform(post("/api/v1/admin/knowledge/docs/{id}/reparse", 1L))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("cancelParse: 超级管理员应成功取消解析")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "knowledge:doc:cancel" })
    void cancelParse_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(knowledgeBaseAppService).cancelParse(any());

        MvcResult result = mockMvc.perform(post("/api/v1/admin/knowledge/docs/{id}/cancel", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("deleteDocument: 超级管理员应成功删除文档")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "knowledge:doc:delete" })
    void deleteDocument_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(knowledgeBaseAppService).deleteDocument(any());

        MvcResult result = mockMvc.perform(delete("/api/v1/admin/knowledge/docs/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("listTags: 超级管理员应返回分页标签列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "knowledge:tag:list" })
    void listTags_asSuperAdmin_shouldReturnPagedTags() throws Exception {
        KnowledgeTagResult.ListItem item = new KnowledgeTagResult.ListItem(1L, "标签", "标签描述", 5);
        when(knowledgeDocQueryService.listTags(any())).thenReturn(new PageImpl<>(List.of(item)));
        when(responseConverter.toTagListPageDTO(any())).thenReturn(tagListPageDTO());

        MvcResult result = mockMvc.perform(
                get("/api/v1/admin/knowledge/tags")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateTagDescription: 超级管理员应成功更新标签描述")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "knowledge:tag:update" })
    void updateTagDescription_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(knowledgeBaseAppService).updateTagDescription(1L, "新描述");

        UpdateTagDescriptionRequestDTO request = new UpdateTagDescriptionRequestDTO("新描述");

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/knowledge/tags/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateTagDescription: 空描述应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "knowledge:tag:update" })
    void updateTagDescription_withBlankDescription_shouldReturn400() throws Exception {
        UpdateTagDescriptionRequestDTO request = new UpdateTagDescriptionRequestDTO("  ");

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/knowledge/tags/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }
}
