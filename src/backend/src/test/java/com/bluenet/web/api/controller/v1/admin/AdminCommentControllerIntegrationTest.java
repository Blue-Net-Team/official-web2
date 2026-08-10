package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.assessment_judgement.CommentResponseConverter;
import com.bluenet.web.api.dto.assessment_judgement.CommentDTO;
import com.bluenet.web.api.dto.assessment_judgement.CommentRequestDTO;
import com.bluenet.web.application.result.comment.CommentResult;
import com.bluenet.web.application.service.CommentAppService;
import com.bluenet.web.domain.exception.DataNotFound;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@DisplayName("AdminCommentController 集成测试")
class AdminCommentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentAppService commentAppService;

    @MockitoBean
    private CommentResponseConverter commentResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private CommentResult commentResult() {
        return CommentResult.builder()
                .id(1L)
                .answerId(1L)
                .userId(SUPER_ADMIN_USER_ID)
                .username("管理员")
                .content("评论内容")
                .score(new BigDecimal("80.00"))
                .commentTime(LocalDateTime.now())
                .build();
    }

    private CommentDTO commentDTO() {
        return CommentDTO.builder()
                .id(1L)
                .answerId(1L)
                .userId(SUPER_ADMIN_USER_ID)
                .username("管理员")
                .content("评论内容")
                .score(new BigDecimal("80.00"))
                .commentTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("listComments: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void listComments_asMember_shouldReturn403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/comments").param("answerId", "1"))
                .andExpect(status().isForbidden())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("listComments: 超级管理员应返回评论列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-comment:query" })
    void listComments_asSuperAdmin_shouldReturnComments() throws Exception {
        when(commentAppService.listComments(1L)).thenReturn(List.of(commentResult()));
        when(commentResponseConverter.toDTO(any(CommentResult.class))).thenReturn(commentDTO());

        MvcResult result = mockMvc.perform(get("/api/v1/admin/comments").param("answerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("addComment: 超级管理员应成功添加评论")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-comment:create" })
    void addComment_asSuperAdmin_shouldReturnComment() throws Exception {
        when(commentAppService.addComment(SUPER_ADMIN_USER_ID, 1L, "评论内容", new BigDecimal("80.00")))
                .thenReturn(commentResult());
        when(commentResponseConverter.toDTO(any(CommentResult.class))).thenReturn(commentDTO());

        CommentRequestDTO request = new CommentRequestDTO();
        request.setAnswerId(1L);
        request.setContent("评论内容");
        request.setScore(new BigDecimal("80.00"));

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("addComment: 缺少答案ID应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-comment:create" })
    void addComment_withMissingAnswerId_shouldReturn400() throws Exception {
        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("评论内容");

        MvcResult result = mockMvc.perform(
                post("/api/v1/admin/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("updateComment: 超级管理员应成功更新评论")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-comment:update" })
    void updateComment_asSuperAdmin_shouldReturnComment() throws Exception {
        when(commentAppService.updateComment(SUPER_ADMIN_USER_ID, 1L, "更新评论", new BigDecimal("90.00")))
                .thenReturn(commentResult());
        when(commentResponseConverter.toDTO(any(CommentResult.class))).thenReturn(commentDTO());

        CommentRequestDTO request = new CommentRequestDTO();
        request.setAnswerId(1L);
        request.setContent("更新评论");
        request.setScore(new BigDecimal("90.00"));

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/comments/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateComment: 评论不存在时应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-comment:update" })
    void updateComment_whenNotFound_shouldReturn404() throws Exception {
        when(commentAppService.updateComment(SUPER_ADMIN_USER_ID, 1L, "更新评论", new BigDecimal("90.00")))
                .thenThrow(new DataNotFound("评论不存在"));

        CommentRequestDTO request = new CommentRequestDTO();
        request.setAnswerId(1L);
        request.setContent("更新评论");
        request.setScore(new BigDecimal("90.00"));

        MvcResult result = mockMvc.perform(
                put("/api/v1/admin/comments/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("deleteComment: 超级管理员应成功删除评论")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "assessment-comment:delete" })
    void deleteComment_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(commentAppService).deleteComment(SUPER_ADMIN_USER_ID, 1L);

        MvcResult result = mockMvc.perform(delete("/api/v1/admin/comments/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
