package com.bluenet.web.api.controller.v1.user;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.api.converter.userinfo.UserInfoResponseConverter;
import com.bluenet.web.application.result.user.UserInfoResult;
import com.bluenet.web.application.service.UserInfoAppService;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("UserInfoController 集成测试")
class UserInfoControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserInfoAppService userInfoAppService;

    @MockitoBean
    private UserInfoResponseConverter userInfoResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void getMyInfo_authenticated_returnsUserInfo() throws Exception {
        UserInfoResult result = new UserInfoResult(
                1L,
                "测试用户",
                "测试昵称",
                "计算机学院",
                "计算机科学与技术",
                "2024",
                "test@example.com",
                100L,
                "MEMBER",
                Direction.COMPUTER_VISION,
                Gender.MALE,
                "简介",
                "github",
                200L,
                "AB7K9L12");
        UserInfo userInfo = UserInfo.builder()
                .id(1L)
                .username("测试用户")
                .email("test@example.com")
                .roleName("MEMBER")
                .build();
        when(userInfoAppService.getMyInfo(1L)).thenReturn(result);
        when(userInfoResponseConverter.toDTO(any(UserInfoResult.class))).thenReturn(userInfo);

        mockMvc.perform(get("/api/v1/user/info/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("测试用户"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.roleName").value("MEMBER"));
    }

    @Test
    void getMyInfo_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/user/info/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.msg").value("未认证"));
    }
}
