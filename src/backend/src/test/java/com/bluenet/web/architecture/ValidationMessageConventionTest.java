package com.bluenet.web.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

class ValidationMessageConventionTest {

    private static final String DTO_BASE_PACKAGE = "com.bluenet.web.api.dto";
    private static final String VALIDATION_PACKAGE = "jakarta.validation.constraints";

    @Test
    void allValidationConstraintsShouldDeclareMessage() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> true);

        List<String> missing = new ArrayList<>();
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents(DTO_BASE_PACKAGE)) {
            Class<?> dtoClass = Class.forName(beanDefinition.getBeanClassName());
            for (Field field : dtoClass.getDeclaredFields()) {
                String location = dtoClass.getName() + "#" + field.getName();
                inspectAnnotations(field.getAnnotations(), location, missing);
                inspectType(field.getAnnotatedType(), location, missing);
            }
        }

        missing.sort(Comparator.naturalOrder());
        assertTrue(
                missing.isEmpty(),
                () -> "Validation constraints missing a non-blank message:\n" + String.join("\n", missing));
    }

    private void inspectType(AnnotatedType type, String location, List<String> missing) {
        if (type instanceof java.lang.reflect.AnnotatedParameterizedType parameterizedType) {
            for (AnnotatedType argument : parameterizedType.getAnnotatedActualTypeArguments()) {
                inspectAnnotations(argument.getAnnotations(), location, missing);
                inspectType(argument, location, missing);
            }
        }
    }

    private void inspectAnnotations(Annotation[] annotations, String location, List<String> missing) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (!VALIDATION_PACKAGE.equals(annotationType.getPackageName())) {
                continue;
            }
            String message = readMessage(annotationType, annotation);
            String defaultMessage = readDefaultMessage(annotationType);
            if (message == null || message.isBlank() || message.equals(defaultMessage)) {
                missing.add(location + " -> @" + annotationType.getSimpleName());
            }
        }
    }

    private String readMessage(Class<? extends Annotation> annotationType, Annotation annotation) {
        try {
            return (String) annotationType.getMethod("message").invoke(annotation);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Cannot read validation message from " + annotationType.getName(),
                    exception);
        }
    }

    private String readDefaultMessage(Class<? extends Annotation> annotationType) {
        try {
            return (String) annotationType.getMethod("message").getDefaultValue();
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("Cannot read default validation message from " + annotationType.getName(),
                    exception);
        }
    }
}
