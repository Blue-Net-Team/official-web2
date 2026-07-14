package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.QrcodeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Qrcode 领域实体单元测试。
 */
@DisplayName("Qrcode 领域实体测试")
class QrcodeTest {

    @Test
    @DisplayName("create: 应创建二维码")
    void create_shouldCreateQrcode() {
        Qrcode qrcode = Qrcode.create(1L, QrcodeType.CONSULTATION);

        assertThat(qrcode.getId()).isNull();
        assertThat(qrcode.getFileId()).isEqualTo(1L);
        assertThat(qrcode.getType()).isEqualTo(QrcodeType.CONSULTATION);
        assertThat(qrcode.getEpoch()).isNull();
        assertThat(qrcode.getDirection()).isNull();
        assertThat(qrcode.getIsShared()).isNull();
    }

    @Test
    @DisplayName("create: 类型为空应抛异常")
    void create_withNullType_shouldThrow() {
        assertThatThrownBy(() -> Qrcode.create(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("二维码类型不能为空");
    }

    @Test
    @DisplayName("forConsultation: 应创建咨询群二维码")
    void forConsultation_shouldCreateConsultationQrcode() {
        Qrcode qrcode = Qrcode.forConsultation(1L);

        assertThat(qrcode.getType()).isEqualTo(QrcodeType.CONSULTATION);
        assertThat(qrcode.getFileId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("forAssessment: 应创建非共用考核群二维码")
    void forAssessment_withNonShared_shouldCreateAssessmentQrcode() {
        Qrcode qrcode = Qrcode.forAssessment(1L, 1, "computer_vision", false);

        assertThat(qrcode.getType()).isEqualTo(QrcodeType.ASSESSMENT);
        assertThat(qrcode.getFileId()).isEqualTo(1L);
        assertThat(qrcode.getEpoch()).isEqualTo(1);
        assertThat(qrcode.getDirection()).isEqualTo("computer_vision");
        assertThat(qrcode.getIsShared()).isFalse();
    }

    @Test
    @DisplayName("forAssessment: 应创建共用考核群二维码")
    void forAssessment_withShared_shouldCreateSharedAssessmentQrcode() {
        Qrcode qrcode = Qrcode.forAssessment(1L, 2, null, true);

        assertThat(qrcode.getType()).isEqualTo(QrcodeType.ASSESSMENT);
        assertThat(qrcode.getEpoch()).isEqualTo(2);
        assertThat(qrcode.getDirection()).isNull();
        assertThat(qrcode.getIsShared()).isTrue();
    }

    @Test
    @DisplayName("forAssessment: 文件ID为空应抛异常")
    void forAssessment_withNullFileId_shouldThrow() {
        assertThatThrownBy(() -> Qrcode.forAssessment(null, 1, "direction", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文件ID不能为空");
    }

    @Test
    @DisplayName("forAssessment: 考核轮次为空或不大于0应抛异常")
    void forAssessment_withInvalidEpoch_shouldThrow() {
        assertThatThrownBy(() -> Qrcode.forAssessment(1L, null, "direction", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("考核轮次不能为空");

        assertThatThrownBy(() -> Qrcode.forAssessment(1L, 0, "direction", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("考核轮次必须为正整数");
    }

    @Test
    @DisplayName("forAssessment: 共用二维码带方向应抛异常")
    void forAssessment_withSharedAndDirection_shouldThrow() {
        assertThatThrownBy(() -> Qrcode.forAssessment(1L, 1, "direction", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("共用二维码时方向必须为空");
    }

    @Test
    @DisplayName("forAssessment: 非共用二维码方向为空应抛异常")
    void forAssessment_withNonSharedAndNullDirection_shouldThrow() {
        assertThatThrownBy(() -> Qrcode.forAssessment(1L, 1, null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("非共用二维码时方向不能为空");
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        Qrcode qrcode = Qrcode.reconstruct(
                100L,
                1L,
                QrcodeType.ASSESSMENT,
                1,
                "embedded",
                false);

        assertThat(qrcode.getId()).isEqualTo(100L);
        assertThat(qrcode.getFileId()).isEqualTo(1L);
        assertThat(qrcode.getType()).isEqualTo(QrcodeType.ASSESSMENT);
        assertThat(qrcode.getEpoch()).isEqualTo(1);
        assertThat(qrcode.getDirection()).isEqualTo("embedded");
        assertThat(qrcode.getIsShared()).isFalse();
    }
}
