package com.bluenet.web.infrastructure.repository.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * PostgreSQL text[] → List<String> 类型处理器
 */
public class PgTextArrayTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType)
            throws SQLException {
        Array array = ps.getConnection().createArrayOf("text", parameter.toArray());
        ps.setArray(i, array);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return extractList(rs.getArray(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return extractList(rs.getArray(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return extractList(cs.getArray(columnIndex));
    }

    @SuppressWarnings("unchecked")
    private List<String> extractList(Array array) throws SQLException {
        if (array == null) {
            return null;
        }
        Object obj = array.getArray();
        if (obj == null) {
            return null;
        }
        if (obj instanceof String[] arr) {
            return Arrays.asList(arr);
        }
        if (obj instanceof Object[] arr) {
            return Arrays.stream(arr)
                    .map(String::valueOf)
                    .toList();
        }
        return Collections.singletonList(String.valueOf(obj));
    }
}
