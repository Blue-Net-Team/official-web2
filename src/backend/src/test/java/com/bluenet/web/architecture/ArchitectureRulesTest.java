package com.bluenet.web.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

/**
 * DDD 分层架构守护规则。
 * <p>
 * 确保 Repository、ApplicationService、DomainService 各层遵守约定的依赖边界。
 * </p>
 */
@AnalyzeClasses(packages = "com.bluenet.web")
public class ArchitectureRulesTest {

    private static final String VO_SUFFIX = "VO";
    private static final String RESPONSE_DTO_CLASS = "com.bluenet.web.api.dto.ResponseDTO";
    private static final String RESPONSE_CONVERTER_CLASS = "com.bluenet.web.api.converter.ResponseConverter";

    @ArchTest
    static final ArchRule repositoryInterfacesShouldNotReturnValueObjects = ArchRuleDefinition
            .methods()
            .that()
            .areDeclaredInClassesThat()
            .areInterfaces()
            .and()
            .areDeclaredInClassesThat()
            .resideInAnyPackage(
                    "com.bluenet.web.domain.repository..",
                    "com.bluenet.web.infrastructure.repository..")
            .should(notReturnTypesWithNameEnding(VO_SUFFIX))
            .because("Repository 接口不得返回名称以 VO 结尾的类型");

    // TODO: 当前
    // AlgorithmJudgeAppServiceImpl、AssessmentQuestionAppServiceImpl、JudgeProblemConfigAdminServiceImpl
    // 直接依赖 Mapper，待重构为 Repository 后取消注释。
    // @ArchTest
    // static final ArchRule applicationServicesShouldNotDependOnMappers =
    // ArchRuleDefinition
    // .noClasses()
    // .that()
    // .resideInAnyPackage(
    // "com.bluenet.web.application.service",
    // "com.bluenet.web.application.service.impl..")
    // .and()
    // .haveSimpleNameEndingWith("Impl")
    // .should()
    // .dependOnClassesThat()
    // .resideInAPackage("com.bluenet.web.infrastructure.repository.mapper..")
    // .because("ApplicationService 实现类不得直接依赖 Mapper 接口，应通过 Repository 访问数据");

    @ArchTest
    static final ArchRule domainServicesShouldNotDependOnApiResponseLayer = ArchRuleDefinition
            .noClasses()
            .that()
            .resideInAnyPackage(
                    "com.bluenet.web.domain.service",
                    "com.bluenet.web.domain.service.impl..")
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName(RESPONSE_DTO_CLASS)
            .orShould()
            .dependOnClassesThat()
            .haveFullyQualifiedName(RESPONSE_CONVERTER_CLASS)
            .because("DomainService 不得依赖 API 层的 ResponseDTO 或 ResponseConverter");

    private static ArchCondition<JavaMethod> notReturnTypesWithNameEnding(String suffix) {
        return new ArchCondition<>("not return types with name ending '" + suffix + "'") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                JavaType returnType = method.getReturnType();
                for (JavaClass involvedRawType : returnType.getAllInvolvedRawTypes()) {
                    if (involvedRawType.getSimpleName().endsWith(suffix)) {
                        events.add(
                                SimpleConditionEvent.violated(
                                        method,
                                        String.format(
                                                "Method %s returns type %s which involves a raw type (%s) ending with '%s'",
                                                method.getFullName(),
                                                returnType.getName(),
                                                involvedRawType.getName(),
                                                suffix)));
                        return;
                    }
                }
            }
        };
    }
}
