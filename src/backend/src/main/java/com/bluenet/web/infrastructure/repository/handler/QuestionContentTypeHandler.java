package com.bluenet.web.infrastructure.repository.handler;

import com.bluenet.web.domain.model.vo.evaluation.QuestionContent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * QuestionContent JSON TypeHandler 支持 PostgreSQL JSONB 类型（使用标准 JDBC）
 */
public class QuestionContentTypeHandler extends BaseTypeHandler<QuestionContent> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, QuestionContent parameter, JdbcType jdbcType)
            throws SQLException {
        try {
            String json = objectMapper.writeValueAsString(parameter);
            ps.setObject(i, json, Types.OTHER);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting QuestionContent to JSON", e);
        }
    }

    @Override
    public QuestionContent getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public QuestionContent getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public QuestionContent getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private QuestionContent parseJson(String json) throws SQLException {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, QuestionContent.class);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON to QuestionContent: " + json, e);
        }
    }
}
