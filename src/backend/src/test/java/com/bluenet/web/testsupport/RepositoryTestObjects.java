package com.bluenet.web.testsupport;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.beans.BeanUtils;

/**
 * 测试夹具专用的 Mapper DO 适配工具，避免测试代码直接把领域实体传给 Mapper。
 */
public final class RepositoryTestObjects {
    private RepositoryTestObjects() {
    }

    public static <D, T> int insert(BaseMapper<D> mapper, T entity, Class<D> dataObjectType) {
        D dataObject = copy(entity, dataObjectType);
        int rows = mapper.insert(dataObject);
        copyInto(dataObject, entity);
        return rows;
    }

    public static <D, T> int updateById(BaseMapper<D> mapper, T entity, Class<D> dataObjectType) {
        D dataObject = copy(entity, dataObjectType);
        int rows = mapper.updateById(dataObject);
        copyInto(dataObject, entity);
        return rows;
    }

    public static <D, T> T toDomain(D dataObject, Class<T> domainType) {
        return copy(dataObject, domainType);
    }

    public static <D> D toDataObject(Object domainObject, Class<D> dataObjectType) {
        return copy(domainObject, dataObjectType);
    }

    private static <T> T copy(Object source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        try {
            var ctor = targetType.getDeclaredConstructor();
            ctor.setAccessible(true);
            T target = ctor.newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to convert object to " + targetType.getSimpleName(), e);
        }
    }

    private static void copyInto(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target);
    }
}
