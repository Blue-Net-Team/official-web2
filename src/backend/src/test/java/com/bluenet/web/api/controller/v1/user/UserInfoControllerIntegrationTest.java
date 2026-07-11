package com.bluenet.web.api.controller.v1.user;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private User saveMemberUser() {
        return UserFixture.member("2024001001").save(userRepository, passwordEncoder);
    }

    @Test
    @WithSecurityPrincipal(roleId = 3L, roleType = "MEMBER")
    void getMyInfo_authenticated_returnsUserInfo() throws Exception {
        User user = saveMemberUser();

        mockMvc.perform(get("/api/v1/user/info/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(user.getId()))
                .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()))
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
