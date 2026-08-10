package com.bluenet.web.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

/**
 * Converter 层架构契约测试。
 * <p>
 * 确保 API ResponseConverter 中的 toDTO 系列方法不直接接收领域实体参数， 强制转换链 Entity → Result →
 * ResponseDTO 的执行。
 * </p>
 */
@AnalyzeClasses(packages = "com.bluenet.web")
public class ConverterLayerArchTest {

    private static final String DOMAIN_ENTITY_PACKAGE = "com.bluenet.web.domain.model.entity";
    private static final String CONVERTER_PACKAGE = "com.bluenet.web.api.converter";
    private static final String DTO_METHOD_NAME_PATTERN = "toDTO|toDTOForUser|toDTOList|toDTOListForUser";

    @ArchTest
    static final ArchRule converterDtoMethodsShouldNotAcceptDomainEntities = ArchRuleDefinition
            .methods()
            .that()
            .arePublic()
            .and()
            .haveNameMatching(DTO_METHOD_NAME_PATTERN)
            .and()
            .areDeclaredInClassesThat()
            .resideInAPackage(CONVERTER_PACKAGE + "..")
            .should(notAcceptDomainEntities())
            .because(
                    "API ResponseConverter 必须遵循 Entity -> Result -> ResponseDTO 转换链，"
                            + "禁止直接接收领域实体参数");

    private static ArchCondition<JavaMethod> notAcceptDomainEntities() {
        return new ArchCondition<>("not accept domain entities as parameters") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                for (JavaClass parameterType : method.getRawParameterTypes()) {
                    if (parameterType.getPackageName().startsWith(DOMAIN_ENTITY_PACKAGE)) {
                        events.add(
                                SimpleConditionEvent.violated(
                                        method,
                                        String.format(
                                                "Method %s accepts domain entity %s as parameter",
                                                method.getFullName(),
                                                parameterType.getName())));
                    }
                }
            }
        };
    }
}
