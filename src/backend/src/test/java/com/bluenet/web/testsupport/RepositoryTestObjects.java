package com.bluenet.web.testsupport;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

/**
 * 测试夹具专用的 Mapper DO 适配工具，避免测试代码直接把领域实体传给 Mapper。
 */
public final class RepositoryTestObjects {
    private RepositoryTestObjects() {
    }

    public static <D, T> int insert(BaseMapper<D> mapper, T entity, Class<D> dataObjectType) {
        D dataObject = RepositoryObjectConverter.copy(entity, dataObjectType);
        int rows = mapper.insert(dataObject);
        RepositoryObjectConverter.copyInto(dataObject, entity);
        return rows;
    }

    public static <D, T> int updateById(BaseMapper<D> mapper, T entity, Class<D> dataObjectType) {
        D dataObject = RepositoryObjectConverter.copy(entity, dataObjectType);
        int rows = mapper.updateById(dataObject);
        RepositoryObjectConverter.copyInto(dataObject, entity);
        return rows;
    }

    public static <D, T> T toDomain(D dataObject, Class<T> domainType) {
        return RepositoryObjectConverter.copy(dataObject, domainType);
    }

    public static <D> D toDataObject(Object domainObject, Class<D> dataObjectType) {
        return RepositoryObjectConverter.copy(domainObject, dataObjectType);
    }
}
