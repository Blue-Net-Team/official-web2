package com.bluenet.web.domain.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserAchievement 领域实体单元测试。
 * <p>
 * UserAchievement 为贫血关联实体，仅验证字段访问行为。
 * </p>
 */
@DisplayName("UserAchievement 领域实体测试")
class UserAchievementTest {

    @Test
    @DisplayName("字段应支持 getter/setter 访问")
    void fields_shouldBeAccessibleViaGetterAndSetter() {
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setId(1L);
        userAchievement.setUserId(2L);
        userAchievement.setAchievementId(3L);

        assertThat(userAchievement.getId()).isEqualTo(1L);
        assertThat(userAchievement.getUserId()).isEqualTo(2L);
        assertThat(userAchievement.getAchievementId()).isEqualTo(3L);
    }
}
