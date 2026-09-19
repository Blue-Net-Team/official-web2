package com.bluenet.web.infrastructure.security.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 敏感字段脱敏工具
 * <p>
 * 在序列化请求参数时将敏感字段值替换为 "***"。脱敏直接在 Jackson 的 {@link JsonNode}
 * 树上原地进行：对象节点按字段名下钻，数组节点逐元素下钻， 因此嵌套对象与数组元素中的敏感字段同样会被脱敏，且不会破坏 JSON 结构。
 * </p>
 */
public class SensitiveFieldFilter {

    private static final String MASK = "***";

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password",
            "newPassword",
            "confirmPassword",
            "verifyCode",
            "resetToken",
            "token");

    private SensitiveFieldFilter() {
    }

    /**
     * 递归对 JSON 节点树脱敏：字段名命中敏感字段集的直接替换为 "***"， 对象节点与数组节点继续下钻。就地修改传入节点并返回同一实例。
     *
     * @param node
     *            待脱敏的 JSON 节点（可为 null）
     * @return 脱敏后的同一节点实例
     */
    public static JsonNode maskSensitiveFields(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isObject()) {
            maskObject((ObjectNode) node);
        } else if (node.isArray()) {
            for (JsonNode element : (ArrayNode) node) {
                maskSensitiveFields(element);
            }
        }
        return node;
    }

    private static void maskObject(ObjectNode objectNode) {
        // 仅修改字段值、不新增或删除字段，因此可以先收集字段名再遍历
        List<String> fieldNames = new ArrayList<>();
        objectNode.fieldNames().forEachRemaining(fieldNames::add);

        for (String fieldName : fieldNames) {
            if (SENSITIVE_FIELDS.contains(fieldName)) {
                objectNode.put(fieldName, MASK);
            } else {
                maskSensitiveFields(objectNode.get(fieldName));
            }
        }
    }

    /**
     * 判断字段名是否为敏感字段
     */
    public static boolean isSensitive(String fieldName) {
        return fieldName != null && SENSITIVE_FIELDS.contains(fieldName);
    }

    public static Set<String> getSensitiveFields() {
        return SENSITIVE_FIELDS;
    }
}
