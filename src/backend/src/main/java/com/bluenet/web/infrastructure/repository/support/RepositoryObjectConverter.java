package com.bluenet.web.infrastructure.repository.support;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * 仓储实现内部的浅拷贝转换工具，集中处理 DO 与领域对象之间的字段搬运。
 */
public final class RepositoryObjectConverter {
    private RepositoryObjectConverter() {
    }

    public static <T> T copy(Object source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetType.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to convert repository object to " + targetType.getSimpleName(), e);
        }
    }

    public static void copyInto(Object source, Object target) {
        if (source != null && target != null) {
            BeanUtils.copyProperties(source, target);
        }
    }

    public static <D, T> int insert(BaseMapper<D> mapper, T domainObject, Class<D> dataObjectType) {
        D dataObject = copy(domainObject, dataObjectType);
        int rows = mapper.insert(dataObject);
        copyInto(dataObject, domainObject);
        return rows;
    }

    public static <D, T> int updateById(BaseMapper<D> mapper, T domainObject, Class<D> dataObjectType) {
        D dataObject = copy(domainObject, dataObjectType);
        int rows = mapper.updateById(dataObject);
        copyInto(dataObject, domainObject);
        return rows;
    }

    public static <D, T> T toDomain(D dataObject, Class<T> domainType) {
        return copy(dataObject, domainType);
    }

    public static <D, T> List<T> toDomainList(List<D> dataObjects, Class<T> domainType) {
        return dataObjects.stream()
                .map(dataObject -> toDomain(dataObject, domainType))
                .toList();
    }
}
