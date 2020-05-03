package com.zhuanbo.service.handler.mapperType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes({List.class})
@Slf4j
public class ListTypeHandler extends BaseTypeHandler<List> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List list, JdbcType jdbcType) throws SQLException {
        try {
            if (list != null && list.size() > 0) {
                String value = this.mapper.writeValueAsString(list);
                preparedStatement.setString(i, value);
            }
        } catch (JsonProcessingException e) {
            log.error("数据库varchar转换list失败：{}",e);
            throw new SQLException();
        }
    }

    @Override
    public List getNullableResult(ResultSet resultSet, String s) throws SQLException {
        List list = new ArrayList();
        String value = resultSet.getString(s);

        try {
            if (!StringUtils.isBlank(value)) {
                list = this.mapper.readValue(value, List.class);
            }
        } catch (IOException e) {
            log.error("数据库varchar转换list失败：{}",e);
            throw new SQLException();
        }
        return list;
    }

    @Override
    public List getNullableResult(ResultSet resultSet, int i) {
        return null;
    }

    @Override
    public List getNullableResult(CallableStatement callableStatement, int i) {
        return null;
    }
}