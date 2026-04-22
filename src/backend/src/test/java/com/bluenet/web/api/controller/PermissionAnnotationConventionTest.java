package com.bluenet.web.api.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;

class PermissionAnnotationConventionTest {

    private static final String CONTROLLER_BASE_PACKAGE = "com.bluenet.web.api.controller";
    private static final Pattern PERMISSION_VALUE_PATTERN = Pattern
            .compile("^[a-z0-9]+(?:-[a-z0-9]+)*(?::[a-z0-9]+(?:-[a-z0-9]+)*)+$");

    @Test
    void allEndpointMethodsShouldDeclareRequiresPermission() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        List<String> missing = new ArrayList<>();

        for (var beanDefinition : scanner.findCandidateComponents(CONTROLLER_BASE_PACKAGE)) {
            Class<?> controllerClass = Class.forName(beanDefinition.getBeanClassName());
            boolean classLevelPermission = AnnotatedElementUtils
                    .hasAnnotation(controllerClass, RequiresPermission.class);

            for (Method method : controllerClass.getDeclaredMethods()) {
                if (!isEndpointMethod(method) || method.isSynthetic()) {
                    continue;
                }
                RequiresPermission permission = AnnotatedElementUtils
                        .findMergedAnnotation(method, RequiresPermission.class);
                if (permission == null) {
                    permission = AnnotatedElementUtils.findMergedAnnotation(controllerClass, RequiresPermission.class);
                }
                if (!classLevelPermission && permission == null) {
                    missing.add(controllerClass.getName() + "#" + method.getName());
                    continue;
                }
                if (permission == null) {
                    continue;
                }
                if (permission.name().isBlank()) {
                    missing.add(controllerClass.getName() + "#" + method.getName() + " -> blank permission name");
                }
                if (!PERMISSION_VALUE_PATTERN.matcher(permission.value()).matches()) {
                    missing.add(
                            controllerClass.getName() + "#" + method.getName()
                                    + " -> invalid permission value: " + permission.value());
                }
                for (String path : resolvePaths(controllerClass, method)) {
                    if (path.startsWith("/api/v1/admin")
                            && permission
                                    .access() != com.bluenet.web.infrastructure.security.annotation.AccessLevel.PROTECTED) {
                        missing.add(
                                controllerClass.getName() + "#" + method.getName()
                                        + " -> admin endpoint must be PROTECTED: " + path);
                    }
                }
            }
        }

        missing.sort(Comparator.naturalOrder());
        assertTrue(
                missing.isEmpty(),
                () -> "Endpoint methods missing @RequiresPermission:\n" + String.join("\n", missing));
    }

    private boolean isEndpointMethod(Method method) {
        return AnnotatedElementUtils.hasAnnotation(method, GetMapping.class)
                || AnnotatedElementUtils.hasAnnotation(method, PostMapping.class)
                || AnnotatedElementUtils.hasAnnotation(method, PutMapping.class)
                || AnnotatedElementUtils.hasAnnotation(method, DeleteMapping.class)
                || AnnotatedElementUtils.hasAnnotation(method, PatchMapping.class)
                || AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class);
    }

    private List<String> resolvePaths(Class<?> controllerClass, Method method) {
        List<String> classPaths = extractPaths(
                AnnotatedElementUtils.findMergedAnnotation(controllerClass, RequestMapping.class));
        List<String> methodPaths = new ArrayList<>();

        methodPaths.addAll(extractPaths(AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class)));
        methodPaths.addAll(extractPaths(AnnotatedElementUtils.findMergedAnnotation(method, GetMapping.class)));
        methodPaths.addAll(extractPaths(AnnotatedElementUtils.findMergedAnnotation(method, PostMapping.class)));
        methodPaths.addAll(extractPaths(AnnotatedElementUtils.findMergedAnnotation(method, PutMapping.class)));
        methodPaths.addAll(extractPaths(AnnotatedElementUtils.findMergedAnnotation(method, DeleteMapping.class)));
        methodPaths.addAll(extractPaths(AnnotatedElementUtils.findMergedAnnotation(method, PatchMapping.class)));

        if (classPaths.isEmpty()) {
            classPaths = List.of("");
        }
        if (methodPaths.isEmpty()) {
            methodPaths = List.of("");
        }

        List<String> paths = new ArrayList<>();
        for (String classPath : classPaths) {
            for (String methodPath : methodPaths) {
                paths.add((classPath + "/" + methodPath).replaceAll("/+", "/"));
            }
        }
        return paths;
    }

    private List<String> extractPaths(RequestMapping mapping) {
        if (mapping == null) {
            return List.of();
        }
        return mapping.path().length > 0 ? List.of(mapping.path()) : List.of(mapping.value());
    }

    private List<String> extractPaths(GetMapping mapping) {
        if (mapping == null) {
            return List.of();
        }
        return mapping.path().length > 0 ? List.of(mapping.path()) : List.of(mapping.value());
    }

    private List<String> extractPaths(PostMapping mapping) {
        if (mapping == null) {
            return List.of();
        }
        return mapping.path().length > 0 ? List.of(mapping.path()) : List.of(mapping.value());
    }

    private List<String> extractPaths(PutMapping mapping) {
        if (mapping == null) {
            return List.of();
        }
        return mapping.path().length > 0 ? List.of(mapping.path()) : List.of(mapping.value());
    }

    private List<String> extractPaths(DeleteMapping mapping) {
        if (mapping == null) {
            return List.of();
        }
        return mapping.path().length > 0 ? List.of(mapping.path()) : List.of(mapping.value());
    }

    private List<String> extractPaths(PatchMapping mapping) {
        if (mapping == null) {
            return List.of();
        }
        return mapping.path().length > 0 ? List.of(mapping.path()) : List.of(mapping.value());
    }
}
