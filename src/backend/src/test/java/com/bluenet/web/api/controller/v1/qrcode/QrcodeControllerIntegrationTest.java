package com.bluenet.web.api.controller.v1.qrcode;

import com.bluenet.web.infrastructure.repository.dataobject.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.qrcode.CreateAssessmentQrcodeRequestDTO;
import com.bluenet.web.api.dto.qrcode.UpdateAssessmentQrcodeRequestDTO;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.api.dto.qrcode.UpdateConsultationQrcodeRequestDTO;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.Qrcode;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.QrcodeType;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.QrcodeMapper;
import com.bluenet.web.infrastructure.security.WithUserVO;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 二维码控制器集成测试
 */
@AutoConfigureMockMvc
@Testcontainers
@Import(TestcontainersConfiguration.class)
class QrcodeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QrcodeMapper qrcodeMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        qrcodeMapper.delete(null);
        fileMapper.delete(null);
    }

    @AfterEach
    void tearDownUserCtx() {
        UserCTX.clear();
    }

    // ==================== 公开接口测试 ====================

    @Test
    @DisplayName("获取咨询群列表 - 空列表应返回200")
    void getConsultationQrcodes_emptyList_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/qrcodes/consultation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("获取咨询群列表 - 有数据应返回列表")
    void getConsultationQrcodes_withData_shouldReturnList() throws Exception {
        // 创建测试文件
        File file1 = createTestFile("qrcode1.png");
        File file2 = createTestFile("qrcode2.png");

        // 创建咨询群二维码
        Qrcode qrcode1 = Qrcode.forConsultation(file1.getId());
        RepositoryTestObjects.insert(qrcodeMapper, qrcode1, QrcodeDO.class);

        Qrcode qrcode2 = Qrcode.forConsultation(file2.getId());
        RepositoryTestObjects.insert(qrcodeMapper, qrcode2, QrcodeDO.class);

        mockMvc.perform(get("/api/v1/qrcodes/consultation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").isNumber())
                .andExpect(jsonPath("$.data[0].fileId").isNumber())
                .andExpect(jsonPath("$.data[1].id").isNumber())
                .andExpect(jsonPath("$.data[1].fileId").isNumber());
    }

    @Test
    @DisplayName("获取咨询群列表 - 应只返回咨询群类型")
    void getConsultationQrcodes_shouldOnlyReturnConsultationType() throws Exception {
        // 创建测试文件
        File file1 = createTestFile("consultation.png");
        File file2 = createTestFile("user.png");

        // 创建咨询群二维码
        Qrcode consultationQrcode = Qrcode.forConsultation(file1.getId());
        RepositoryTestObjects.insert(qrcodeMapper, consultationQrcode, QrcodeDO.class);

        // 创建用户二维码（不应被查询出来）
        Qrcode userQrcode = Qrcode.reconstruct(null, file2.getId(), QrcodeType.USER, null, null, null);
        RepositoryTestObjects.insert(qrcodeMapper, userQrcode, QrcodeDO.class);

        mockMvc.perform(get("/api/v1/qrcodes/consultation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("获取咨询群列表 - 应按ID升序排列")
    void getConsultationQrcodes_shouldBeOrderedById() throws Exception {
        // 创建测试文件
        File file1 = createTestFile("qrcode1.png");
        File file2 = createTestFile("qrcode2.png");
        File file3 = createTestFile("qrcode3.png");

        // 按顺序插入（ID会按插入顺序递增）
        Qrcode first = Qrcode.forConsultation(file1.getId());
        RepositoryTestObjects.insert(qrcodeMapper, first, QrcodeDO.class);

        Qrcode second = Qrcode.forConsultation(file2.getId());
        RepositoryTestObjects.insert(qrcodeMapper, second, QrcodeDO.class);

        Qrcode third = Qrcode.forConsultation(file3.getId());
        RepositoryTestObjects.insert(qrcodeMapper, third, QrcodeDO.class);

        // 验证返回结果按ID升序排列
        mockMvc.perform(get("/api/v1/qrcodes/consultation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].id").value(first.getId()))
                .andExpect(jsonPath("$.data[1].id").value(second.getId()))
                .andExpect(jsonPath("$.data[2].id").value(third.getId()));
    }

    // ==================== 管理接口测试 - 创建 ====================

    @Test
    @DisplayName("创建咨询群二维码 - 超管应成功")
    @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "SUPER_ADMIN")
    void createConsultationQrcode_asAdmin_shouldSucceed() throws Exception {
        File file = createTestFile("qrcode.png");

        mockMvc.perform(
                post("/api/v1/admin/qrcodes/consultation")
                        .param("fileId", file.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("创建咨询群二维码 - 未登录应返回401")
    void createConsultationQrcode_notAuthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(
                post("/api/v1/admin/qrcodes/consultation")
                        .param("fileId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("创建咨询群二维码 - 普通用户应返回403")
    @WithUserVO(userId = 2L, studentId = "2024001002", username = "普通用户", roleName = "CANDIDATE")
    void createConsultationQrcode_asCandidate_shouldReturn403() throws Exception {
        mockMvc.perform(
                post("/api/v1/admin/qrcodes/consultation")
                        .param("fileId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("创建咨询群二维码 - 文件不存在应返回404")
    @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "SUPER_ADMIN")
    void createConsultationQrcode_fileNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(
                post("/api/v1/admin/qrcodes/consultation")
                        .param("fileId", "99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("创建咨询群二维码 - 文件类型不匹配应返回400")
    @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "SUPER_ADMIN")
    void createConsultationQrcode_fileTypeMismatch_shouldReturn400() throws Exception {
        File file = createTestFileWithType("avatar.png", FileType.AVATAR);

        mockMvc.perform(
                post("/api/v1/admin/qrcodes/consultation")
                        .param("fileId", file.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    // ==================== 管理接口测试 - 删除 ====================

    @Test
    @DisplayName("删除咨询群二维码 - 超管应成功")
    @WithUserVO(userId = 3L, studentId = "2024001003", username = "管理员", roleName = "SUPER_ADMIN")
    void deleteConsultationQrcode_asAdmin_shouldSucceed() throws Exception {
        // 创建测试文件和二维码
        File file = createTestFile("to-delete.png");
        Qrcode qrcode = Qrcode.forConsultation(file.getId());
        RepositoryTestObjects.insert(qrcodeMapper, qrcode, QrcodeDO.class);

        mockMvc.perform(delete("/api/v1/admin/qrcodes/consultation/" + qrcode.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证已删除
        Qrcode deleted = RepositoryTestObjects.toDomain(qrcodeMapper.selectById(qrcode.getId()), Qrcode.class);
        assertNull(deleted);
    }

    @Test
    @DisplayName("删除咨询群二维码 - 不存在应返回404")
    @WithUserVO(userId = 4L, studentId = "2024001004", username = "管理员", roleName = "SUPER_ADMIN")
    void deleteConsultationQrcode_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/qrcodes/consultation/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("删除咨询群二维码 - 未登录应返回401")
    void deleteConsultationQrcode_notAuthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/qrcodes/consultation/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("删除咨询群二维码 - 普通用户应返回403")
    @WithUserVO(userId = 5L, studentId = "2024001005", username = "普通用户", roleName = "CANDIDATE")
    void deleteConsultationQrcode_asCandidate_shouldReturn403() throws Exception {
        // 创建测试文件和二维码
        File file = createTestFile("qrcode.png");
        Qrcode qrcode = Qrcode.forConsultation(file.getId());
        RepositoryTestObjects.insert(qrcodeMapper, qrcode, QrcodeDO.class);

        mockMvc.perform(delete("/api/v1/admin/qrcodes/consultation/" + qrcode.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("删除咨询群二维码 - 删除用户类型二维码应失败")
    @WithUserVO(userId = 6L, studentId = "2024001006", username = "管理员", roleName = "SUPER_ADMIN")
    void deleteConsultationQrcode_deleteUserType_shouldFail() throws Exception {
        // 创建用户类型二维码
        File file = createTestFile("user-qrcode.png");
        Qrcode userQrcode = Qrcode.reconstruct(null, file.getId(), QrcodeType.USER, null, null, null);
        RepositoryTestObjects.insert(qrcodeMapper, userQrcode, QrcodeDO.class);

        mockMvc.perform(delete("/api/v1/admin/qrcodes/consultation/" + userQrcode.getId()))
                .andExpect(status().isBadRequest());
    }

    // ==================== 辅助方法 ====================

    private File createTestFile(String filename) {
        return createTestFileWithType(filename, FileType.QRCODE);
    }

    private File createTestFileWithType(String filename, FileType type) {
        File file = File.reconstruct(null, filename, type, "test-url/" + filename);
        RepositoryTestObjects.insert(fileMapper, file, FileDO.class);
        return file;
    }

    // ==================== 管理端咨询群二维码测试 - 更新 ====================

    @Test
    @DisplayName("更新咨询群二维码 - 超管应成功")
    @WithUserVO(userId = 7L, studentId = "2024001007", username = "管理员", roleName = "SUPER_ADMIN")
    void updateConsultationQrcode_asAdmin_shouldSucceed() throws Exception {
        // 创建原始文件和二维码
        File oldFile = createTestFile("old-qrcode.png");
        Qrcode qrcode = Qrcode.forConsultation(oldFile.getId());
        RepositoryTestObjects.insert(qrcodeMapper, qrcode, QrcodeDO.class);

        // 创建新文件
        File newFile = createTestFile("new-qrcode.png");

        // 构建请求体
        UpdateConsultationQrcodeRequestDTO request = UpdateConsultationQrcodeRequestDTO.builder()
                .fileId(newFile.getId())
                .build();

        mockMvc.perform(
                put("/api/v1/admin/qrcodes/consultation/" + qrcode.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("管理端获取咨询群二维码列表 - 超管应成功")
    @WithUserVO(userId = 8L, studentId = "2024001008", username = "管理员", roleName = "SUPER_ADMIN")
    void getConsultationQrcodesAdmin_asAdmin_shouldSucceed() throws Exception {
        // 创建测试数据
        File file = createTestFile("qrcode.png");
        Qrcode qrcode = Qrcode.forConsultation(file.getId());
        RepositoryTestObjects.insert(qrcodeMapper, qrcode, QrcodeDO.class);

        mockMvc.perform(get("/api/v1/admin/qrcodes/consultation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ==================== 管理端考核群二维码测试 ====================

    @Test
    @DisplayName("获取考核群二维码列表 - 超管应成功")
    @WithUserVO(userId = 9L, studentId = "2024001009", username = "管理员", roleName = "SUPER_ADMIN")
    void getAssessmentQrcodes_asAdmin_shouldSucceed() throws Exception {
        // 创建测试数据
        File file = createTestFile("assessment-qrcode.png");
        Qrcode qrcode = Qrcode.forAssessment(file.getId(), 1, "COMPUTER_VISION", false);
        RepositoryTestObjects.insert(qrcodeMapper, qrcode, QrcodeDO.class);

        mockMvc.perform(get("/api/v1/admin/qrcodes/assessment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("创建考核群二维码 - 超管应成功")
    @WithUserVO(userId = 10L, studentId = "2024001010", username = "管理员", roleName = "SUPER_ADMIN")
    void createAssessmentQrcode_asAdmin_shouldSucceed() throws Exception {
        File file = createTestFile("assessment-qrcode.png");

        CreateAssessmentQrcodeRequestDTO request = CreateAssessmentQrcodeRequestDTO.builder()
                .fileId(file.getId())
                .direction(Direction.COMPUTER_VISION)
                .epoch(1)
                .isShared(false)
                .build();

        mockMvc.perform(
                post("/api/v1/admin/qrcodes/assessment")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("创建共用考核群二维码 - 超管应成功")
    @WithUserVO(userId = 11L, studentId = "2024001011", username = "管理员", roleName = "SUPER_ADMIN")
    void createSharedAssessmentQrcode_asAdmin_shouldSucceed() throws Exception {
        File file = createTestFile("shared-qrcode.png");

        CreateAssessmentQrcodeRequestDTO request = CreateAssessmentQrcodeRequestDTO.builder()
                .fileId(file.getId())
                .epoch(1)
                .isShared(true)
                .build();

        mockMvc.perform(
                post("/api/v1/admin/qrcodes/assessment")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("更新考核群二维码 - 超管应成功")
    @WithUserVO(userId = 12L, studentId = "2024001012", username = "管理员", roleName = "SUPER_ADMIN")
    void updateAssessmentQrcode_asAdmin_shouldSucceed() throws Exception {
        // 创建原始数据
        File oldFile = createTestFile("old-assessment-qrcode.png");
        Qrcode qrcode = Qrcode.forAssessment(oldFile.getId(), 1, "COMPUTER_VISION", false);
        RepositoryTestObjects.insert(qrcodeMapper, qrcode, QrcodeDO.class);

        // 创建新文件
        File newFile = createTestFile("new-assessment-qrcode.png");

        UpdateAssessmentQrcodeRequestDTO request = UpdateAssessmentQrcodeRequestDTO.builder()
                .fileId(newFile.getId())
                .direction(Direction.STRUCTURAL_DESIGN)
                .epoch(2)
                .isShared(false)
                .build();

        mockMvc.perform(
                put("/api/v1/admin/qrcodes/assessment/" + qrcode.getId())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除考核群二维码 - 超管应成功")
    @WithUserVO(userId = 13L, studentId = "2024001013", username = "管理员", roleName = "SUPER_ADMIN")
    void deleteAssessmentQrcode_asAdmin_shouldSucceed() throws Exception {
        // 创建测试数据
        File file = createTestFile("assessment-qrcode.png");
        Qrcode qrcode = Qrcode.forAssessment(file.getId(), 1, "COMPUTER_VISION", false);
        RepositoryTestObjects.insert(qrcodeMapper, qrcode, QrcodeDO.class);

        mockMvc.perform(delete("/api/v1/admin/qrcodes/assessment/" + qrcode.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证已删除
        Qrcode deleted = RepositoryTestObjects.toDomain(qrcodeMapper.selectById(qrcode.getId()), Qrcode.class);
        assertNull(deleted);
    }
}
