package com.bluenet.web.infrastructure.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TemplateVariableSubstitutorTest {

    @Test
    @DisplayName("Should substitute single variable")
    void substitute_singleVariable() {
        String template = "Your code is {{code}}";
        Map<String, String> variables = Map.of("code", "123456");

        String result = TemplateVariableSubstitutor.substitute(template, variables);

        assertEquals("Your code is 123456", result);
    }

    @Test
    @DisplayName("Should substitute multiple variables")
    void substitute_multipleVariables() {
        String template = "Hello {{username}}, your code is {{code}}";
        Map<String, String> variables = Map.of("username", "John", "code", "123456");

        String result = TemplateVariableSubstitutor.substitute(template, variables);

        assertEquals("Hello John, your code is 123456", result);
    }

    @Test
    @DisplayName("Should leave placeholder unchanged when variable is missing")
    void substitute_missingVariable() {
        String template = "Hello {{username}}, your code is {{code}}";
        Map<String, String> variables = Map.of("username", "John");

        String result = TemplateVariableSubstitutor.substitute(template, variables);

        assertEquals("Hello John, your code is {{code}}", result);
    }

    @Test
    @DisplayName("Should return null when template is null")
    void substitute_nullTemplate() {
        String result = TemplateVariableSubstitutor.substitute(null, Map.of("key", "value"));

        assertNull(result);
    }

    @Test
    @DisplayName("Should return original template when variables is null")
    void substitute_nullVariables() {
        String template = "Hello {{username}}";

        String result = TemplateVariableSubstitutor.substitute(template, null);

        assertEquals(template, result);
    }

    @Test
    @DisplayName("Should return original template when variables is empty")
    void substitute_emptyVariables() {
        String template = "Hello {{username}}";

        String result = TemplateVariableSubstitutor.substitute(template, Map.of());

        assertEquals(template, result);
    }

    @Test
    @DisplayName("Should substitute same variable multiple times")
    void substitute_sameVariableMultipleTimes() {
        String template = "{{name}} {{name}} {{name}}";
        Map<String, String> variables = Map.of("name", "Alice");

        String result = TemplateVariableSubstitutor.substitute(template, variables);

        assertEquals("Alice Alice Alice", result);
    }

    @Test
    @DisplayName("Should handle template without variables")
    void substitute_noVariables() {
        String template = "Hello World";

        String result = TemplateVariableSubstitutor.substitute(template, Map.of("key", "value"));

        assertEquals("Hello World", result);
    }
}
