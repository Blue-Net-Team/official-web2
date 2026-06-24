package com.bluenet.web.api.converter.wpsform;

import com.bluenet.web.api.dto.wps.WpsCreateAnswerCallbackRequestDTO;
import com.bluenet.web.application.command.wpsform.WpsFormCommands;
import com.bluenet.web.domain.model.vo.wps.WpsFormField;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WPS 表单请求转换器。
 * <p>
 * 负责将 API 层的 WPS 回调 DTO 转换为应用层命令。
 * </p>
 */
@Component
public class WpsFormRequestConverter {

    /**
     * 将 create_answer 回调 DTO 转换为创建用户命令。
     *
     * @param request
     *            WPS create_answer 回调请求 DTO
     * @return 创建用户命令
     */
    public WpsFormCommands.CreateUserFromWpsFormCommand toCreateUserCommand(WpsCreateAnswerCallbackRequestDTO request) {
        List<WpsCreateAnswerCallbackRequestDTO.AnswerContent> answers = request.getAnswerContents();
        if (answers == null || answers.isEmpty()) {
            return null;
        }

        Map<String, String> fieldMap = answers.stream()
                .filter(a -> a.getTitle() != null && a.getValue() != null)
                .collect(
                        Collectors.toMap(
                                WpsCreateAnswerCallbackRequestDTO.AnswerContent::getTitle,
                                a -> valueToString(a.getValue()),
                                (a, b) -> a));

        return new WpsFormCommands.CreateUserFromWpsFormCommand(
                fieldMap.get(WpsFormField.STUDENT_ID),
                fieldMap.get(WpsFormField.USERNAME),
                fieldMap.get(WpsFormField.EMAIL),
                fieldMap.get(WpsFormField.DIRECTION),
                fieldMap.get(WpsFormField.MAJOR),
                fieldMap.get(WpsFormField.COLLEGE),
                fieldMap.get(WpsFormField.GENDER));
    }

    /**
     * 将 WPS 返回值转为字符串，处理字符串和数组两种类型（如多选返回 ["a","b"]）。
     */
    private static String valueToString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            return s;
        }
        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.joining(","));
        }
        return value.toString();
    }
}
