package com.bluenet.web.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.bluenet.web.api.dto.assessment_judgement.CommentRequestDTO;
import com.bluenet.web.api.dto.assessment_judgement.FinalizeScoreRequestDTO;
import com.bluenet.web.api.dto.judge.ConfirmJudgeLanguageLimitRequestDTO;
import com.bluenet.web.api.dto.judge.UpsertJudgeProblemConfigRequestDTO;
import com.bluenet.web.api.dto.knowledge.UpdateTagDescriptionRequestDTO;
import com.bluenet.web.api.dto.knowledge.UploadKnowledgeDocRequestDTO;
import com.bluenet.web.api.dto.softwareresource.SoftwareResourceListRequestDTO;

class RequestValidationMessageTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void assessmentRequestsShouldIdentifyRequiredFields() {
        CommentRequestDTO comment = new CommentRequestDTO();
        FinalizeScoreRequestDTO finalizeScore = new FinalizeScoreRequestDTO();

        assertThat(validator.validate(comment))
                .extracting(violation -> violation.getMessage())
                .containsExactly("答案ID不能为空");
        assertThat(validator.validate(finalizeScore))
                .extracting(violation -> violation.getMessage())
                .containsExactlyInAnyOrder("答案ID不能为空", "最终得分不能为空");
    }

    @Test
    void languageLimitShouldIdentifyRequiredFields() {
        ConfirmJudgeLanguageLimitRequestDTO request = new ConfirmJudgeLanguageLimitRequestDTO(null, null, null);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getMessage())
                .containsExactlyInAnyOrder("时间限制不能为空", "内存限制不能为空", "输出限制不能为空");
    }

    @Test
    void languageLimitShouldDescribeMinimumValuesAndUnits() {
        ConfirmJudgeLanguageLimitRequestDTO request = new ConfirmJudgeLanguageLimitRequestDTO(0, 0, 0);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getMessage())
                .containsExactlyInAnyOrder(
                        "时间限制必须大于等于1毫秒",
                        "内存限制必须大于等于1KB",
                        "输出限制必须大于等于1KB");
    }

    @Test
    void judgeConfigShouldIdentifyRequiredTopLevelFields() {
        UpsertJudgeProblemConfigRequestDTO request = new UpsertJudgeProblemConfigRequestDTO(
                null, " ", null, null, null, null, null, List.of(), null);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getMessage())
                .containsExactlyInAnyOrder(
                        "生成器语言不能为空",
                        "生成器源码不能为空",
                        "主标准解语言不能为空",
                        "基准测试重复次数不能为空",
                        "时限倍率不能为空",
                        "最小额外时间不能为空",
                        "取整粒度不能为空",
                        "标准解列表不能为空",
                        "测试用例列表不能为空");
    }

    @Test
    void judgeConfigShouldDescribeNumericBoundaries() {
        UpsertJudgeProblemConfigRequestDTO request = validJudgeConfigBuilder(
                0,
                new BigDecimal("0.9"),
                -1,
                0,
                List.of(new UpsertJudgeProblemConfigRequestDTO.StandardSolutionRequest("python", "print(1)", true)),
                List.of(
                        new UpsertJudgeProblemConfigRequestDTO.TestcaseConfigRequest(
                                1, "SAMPLE", null, BigDecimal.ONE, false, true, null)));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getMessage())
                .containsExactlyInAnyOrder(
                        "基准测试重复次数必须大于等于1",
                        "时限倍率必须大于等于1.0",
                        "最小额外时间必须大于等于0毫秒",
                        "取整粒度必须大于等于1毫秒");
    }

    @Test
    void judgeConfigShouldValidateNestedRequiredFields() {
        UpsertJudgeProblemConfigRequestDTO.StandardSolutionRequest solution = new UpsertJudgeProblemConfigRequestDTO.StandardSolutionRequest(
                null, null, true);
        UpsertJudgeProblemConfigRequestDTO.TestcaseConfigRequest testcase = new UpsertJudgeProblemConfigRequestDTO.TestcaseConfigRequest(
                null, null, null, null, false, true, null);
        UpsertJudgeProblemConfigRequestDTO request = validJudgeConfigBuilder(
                5,
                new BigDecimal("1.2"),
                100,
                10,
                List.of(solution),
                List.of(testcase));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getMessage())
                .containsExactlyInAnyOrder(
                        "标准解语言不能为空",
                        "标准解源码不能为空",
                        "测试用例序号不能为空",
                        "测试用例分类不能为空",
                        "测试用例权重不能为空");
    }

    @Test
    void judgeConfigShouldValidateNestedBlankFieldsAndBoundaries() {
        UpsertJudgeProblemConfigRequestDTO.StandardSolutionRequest solution = new UpsertJudgeProblemConfigRequestDTO.StandardSolutionRequest(
                " ", "", true);
        UpsertJudgeProblemConfigRequestDTO.TestcaseConfigRequest testcase = new UpsertJudgeProblemConfigRequestDTO.TestcaseConfigRequest(
                0, " ", null, new BigDecimal("-0.1"), false, true, null);
        UpsertJudgeProblemConfigRequestDTO request = validJudgeConfigBuilder(
                5,
                new BigDecimal("1.2"),
                100,
                10,
                List.of(solution),
                List.of(testcase));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getMessage())
                .containsExactlyInAnyOrder(
                        "标准解语言不能为空",
                        "标准解源码不能为空",
                        "测试用例序号必须大于等于1",
                        "测试用例分类不能为空",
                        "测试用例权重必须大于等于0.0");
    }

    @Test
    void knowledgeRequestsShouldIdentifyRequiredFields() {
        assertThat(validator.validate(new UpdateTagDescriptionRequestDTO(" ")))
                .extracting(violation -> violation.getMessage())
                .containsExactly("标签描述不能为空");
        assertThat(validator.validate(new UploadKnowledgeDocRequestDTO(null, null)))
                .extracting(violation -> violation.getMessage())
                .containsExactly("Markdown 文件不能为空");

        MockMultipartFile file = new MockMultipartFile("file", "doc.md", "text/markdown", new byte[] { 1 });
        assertThat(validator.validate(new UploadKnowledgeDocRequestDTO(null, file))).isEmpty();
    }

    @Test
    void softwareResourceQueryShouldDescribePaginationBoundaries() {
        SoftwareResourceListRequestDTO request = SoftwareResourceListRequestDTO.builder()
                .page(-1)
                .size(101)
                .build();
        SoftwareResourceListRequestDTO zeroSizeRequest = SoftwareResourceListRequestDTO.builder()
                .page(0)
                .size(0)
                .build();

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getMessage())
                .containsExactlyInAnyOrder("页码必须大于等于0", "每页大小必须小于等于100");
        assertThat(validator.validate(zeroSizeRequest))
                .extracting(violation -> violation.getMessage())
                .containsExactly("每页大小必须大于等于1");
    }

    private UpsertJudgeProblemConfigRequestDTO validJudgeConfigBuilder(
            Integer benchmarkRepeatTimes,
            BigDecimal marginMultiplier,
            Integer minExtraMs,
            Integer roundToMs,
            List<UpsertJudgeProblemConfigRequestDTO.StandardSolutionRequest> solutions,
            List<UpsertJudgeProblemConfigRequestDTO.TestcaseConfigRequest> testcases) {
        return new UpsertJudgeProblemConfigRequestDTO(
                "python",
                "print(1)",
                "python",
                benchmarkRepeatTimes,
                marginMultiplier,
                minExtraMs,
                roundToMs,
                solutions,
                testcases);
    }
}
