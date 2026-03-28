package com.bluenet.web.infrastructure.config.converter;

import com.baomidou.mybatisplus.annotation.EnumValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * 枚举转换器工厂
 * <p>
 * 用于将字符串转换为枚举类型，支持MyBatis-Plus的@EnumValue注解。
 * 如果枚举字段有@EnumValue注解，则根据注解值进行转换；否则根据枚举名称进行转换。
 * </p>
 */
public class EnumConverterFactory implements ConverterFactory<String, Enum<?>> {

    @Override
    public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    private static class StringToEnumConverter<T extends Enum<?>> implements Converter<String, T> {
        private final Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            if (source == null || source.isEmpty()) {
                return null;
            }

            // 尝试根据@EnumValue注解的值进行转换
            T[] enumConstants = enumType.getEnumConstants();
            for (T constant : enumConstants) {
                Field field = ReflectionUtils.findField(enumType, "value");
                if (field != null && field.isAnnotationPresent(EnumValue.class)) {
                    field.setAccessible(true);
                    Object value = ReflectionUtils.getField(field, constant);
                    if (source.equals(value.toString())) {
                        return constant;
                    }
                }
            }

            // 如果没有@EnumValue注解或未找到匹配值，则尝试根据枚举名称进行转换
            try {
                // 使用反射调用Enum.valueOf方法，避免泛型类型约束问题
                return (T) Enum.class.getMethod("valueOf", Class.class, String.class)
                        .invoke(
                                null,
                                enumType,
                                source.toUpperCase());
            } catch (Exception e) {
                // 如果仍然失败，尝试不区分大小写查找
                for (T constant : enumConstants) {
                    if (constant.name().equalsIgnoreCase(source)) {
                        return constant;
                    }
                }
                if (e.getCause() instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e.getCause();
                }
                throw new IllegalArgumentException("Cannot convert " + source + " to " + enumType.getSimpleName(), e);
            }
        }
    }
}
