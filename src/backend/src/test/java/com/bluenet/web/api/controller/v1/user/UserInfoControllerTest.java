package com.bluenet.web.api.controller.v1.user;

import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.application.service.UserInfoService;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.infrastructure.security.WithUserVO;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class UserInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserInfoService userInfoService;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @WithUserVO(userId = 1L, studentId = "2024001001", username = "张三", roleName = "MEMBER", permissions = "user:info:me")
    void getMyInfo_whenAuthenticated_returnsUserInfo() throws Exception {
        UserInfo expected = UserInfo.builder()
                .id(1L)
                .username("张三")
                .college("计算机学院")
                .major("软件工程")
                .grade("2024")
                .email("zhangsan@example.com")
                .avatarFileId(null)
                .roleName("MEMBER")
                .direction(Direction.COMPUTER_VISION)
                .gender(Gender.UNKNOWN)
                .build();

        when(userInfoService.getMyInfo()).thenReturn(expected);

        mockMvc.perform(get("/api/v1/user/info/me").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data.username").value("张三"))
                .andExpect(jsonPath("$.data.roleName").value("MEMBER"));

        verify(userInfoService).getMyInfo();
    }

    @Test
    @WithUserVO(permissions = "user:info:me")
    void getMyInfo_whenServiceThrowsUnauthorized_returnsErrorResponse() throws Exception {
        when(userInfoService.getMyInfo()).thenThrow(new Unauthorized("未认证"));

        mockMvc.perform(get("/api/v1/user/info/me").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()) // 控制器捕获异常后返回
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.msg").value("未认证"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(userInfoService).getMyInfo();
    }
}
