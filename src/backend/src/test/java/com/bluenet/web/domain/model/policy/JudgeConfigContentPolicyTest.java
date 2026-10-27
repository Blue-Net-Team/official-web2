package com.bluenet.web.domain.model.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JudgeConfigContentPolicy 单元测试。
 */
@DisplayName("JudgeConfigContentPolicy 判题内容指纹测试")
class JudgeConfigContentPolicyTest {

    @Test
    @DisplayName("fingerprint: 标准解与用例顺序不影响结果")
    void fingerprint_shouldIgnoreListOrder() {
        String first = fingerprint(
                List.of(
                        new JudgeConfigContentPolicy.StandardSolutionContent("python", "h1", false),
                        new JudgeConfigContentPolicy.StandardSolutionContent("cpp", "h2", true)),
                List.of(
                        new JudgeConfigContentPolicy.TestcaseContent(1, "SAMPLE", "{\"n\":1}", new BigDecimal("1"),
                                true, false, null),
                        new JudgeConfigContentPolicy.TestcaseContent(2, "NORMAL", "{\"n\":2}", new BigDecimal("2"),
                                true, false, "大用例")));

        String second = fingerprint(
                List.of(
                        new JudgeConfigContentPolicy.StandardSolutionContent("cpp", "h2", true),
                        new JudgeConfigContentPolicy.StandardSolutionContent("python", "h1", false)),
                List.of(
                        new JudgeConfigContentPolicy.TestcaseContent(2, "NORMAL", "{\"n\":2}", new BigDecimal("2.00"),
                                true, false, "大用例"),
                        new JudgeConfigContentPolicy.TestcaseContent(1, "SAMPLE", "{\"n\":1}", new BigDecimal("1.0"),
                                true, false, null)));

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("fingerprint: 数值表示差异不应被判为变更")
    void fingerprint_shouldNormalizeDecimals() {
        String first = JudgeConfigContentPolicy.fingerprint(
                "python",
                "hash",
                "cpp",
                5,
                new BigDecimal("1.2"),
                50,
                50,
                List.of(),
                List.of());
        String second = JudgeConfigContentPolicy.fingerprint(
                "python",
                "hash",
                "cpp",
                5,
                new BigDecimal("1.20"),
                50,
                50,
                List.of(),
                List.of());

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("fingerprint: generator 源码变更应改变指纹")
    void fingerprint_shouldDetectGeneratorChange() {
        String base = fingerprint("hash-1", List.of(), List.of());
        String changed = fingerprint("hash-2", List.of(), List.of());

        assertThat(base).isNotEqualTo(changed);
    }

    @Test
    @DisplayName("fingerprint: 标准解源码变更应改变指纹")
    void fingerprint_shouldDetectStandardSolutionChange() {
        String base = fingerprint(
                "hash",
                List.of(new JudgeConfigContentPolicy.StandardSolutionContent("python", "solution-hash-1", true)),
                List.of());
        String changed = fingerprint(
                "hash",
                List.of(new JudgeConfigContentPolicy.StandardSolutionContent("python", "solution-hash-2", true)),
                List.of());

        assertThat(base).isNotEqualTo(changed);
    }

    @Test
    @DisplayName("fingerprint: 测试用例参数变更应改变指纹")
    void fingerprint_shouldDetectTestcaseChange() {
        String base = fingerprint(
                "hash",
                List.of(),
                List.of(
                        new JudgeConfigContentPolicy.TestcaseContent(1, "SAMPLE", "{\"n\":1}", BigDecimal.ONE, true,
                                false, null)));
        String changed = fingerprint(
                "hash",
                List.of(),
                List.of(
                        new JudgeConfigContentPolicy.TestcaseContent(1, "SAMPLE", "{\"n\":2}", BigDecimal.ONE, true,
                                false, null)));

        assertThat(base).isNotEqualTo(changed);
    }

    private String fingerprint(
            String generatorHash,
            List<JudgeConfigContentPolicy.StandardSolutionContent> solutions,
            List<JudgeConfigContentPolicy.TestcaseContent> testcases) {
        return JudgeConfigContentPolicy.fingerprint(
                "python",
                generatorHash,
                "python",
                5,
                new BigDecimal("1.2"),
                50,
                50,
                solutions,
                testcases);
    }

    private String fingerprint(
            List<JudgeConfigContentPolicy.StandardSolutionContent> solutions,
            List<JudgeConfigContentPolicy.TestcaseContent> testcases) {
        return JudgeConfigContentPolicy.fingerprint(
                "python",
                "generator-hash",
                "cpp",
                5,
                new BigDecimal("1.2"),
                50,
                50,
                solutions,
                testcases);
    }
}
