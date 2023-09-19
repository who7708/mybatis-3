package org.apache.my;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Chris
 * @version 1.0.0
 * @since 2023.09.20 020
 */
// @MappedTypes()
// @MappedJdbcTypes()
public class TinyIntTypeHandler implements TypeHandler<Integer> {
    @Override
    public void setParameter(PreparedStatement ps, int i, Integer parameter, JdbcType jdbcType) throws SQLException {

    }

    @Override
    public Integer getResult(ResultSet rs, String columnName) throws SQLException {
        return null;
    }

    @Override
    public Integer getResult(ResultSet rs, int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Integer getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }
}
