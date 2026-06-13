package com.manzhushaka.db.crypto;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用于实体字段透明加解密。
 * 实体类需要开启 {@code @TableName(autoResultMap = true)}，
 * 自定义 XML 查询需要使用 {@code resultMap} 明确声明加密字段映射。
 */
@MappedTypes(String.class)
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
public class EncryptStringTypeHandler extends BaseTypeHandler<String> {
    private static volatile DbFieldCryptoService cryptoService;

    /**
     * 设置 cryptoService。
     *
     * @param cryptoService cryptoService 参数
     */
    static void setCryptoService(DbFieldCryptoService cryptoService) {
        EncryptStringTypeHandler.cryptoService = cryptoService;
    }

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int index, String parameter, JdbcType jdbcType)
        throws SQLException {
        preparedStatement.setString(index, requireCryptoService().encrypt(parameter));
    }

    /**
     * 返回 nullableResult。
     *
     * @param resultSet resultSet 参数
     * @param columnName columnName 参数
     * @return 字段值
     */
    @Override
    public String getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return decrypt(resultSet.getString(columnName));
    }

    /**
     * 返回 nullableResult。
     *
     * @param resultSet resultSet 参数
     * @param columnIndex columnIndex 参数
     * @return 字段值
     */
    @Override
    public String getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return decrypt(resultSet.getString(columnIndex));
    }

    /**
     * 返回 nullableResult。
     *
     * @param callableStatement callableStatement 参数
     * @param columnIndex columnIndex 参数
     * @return 字段值
     */
    @Override
    public String getNullableResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        return decrypt(callableStatement.getString(columnIndex));
    }

    /**
     * 执行 decrypt 逻辑。
     *
     * @param rawValue rawValue 参数
     * @return 处理结果
     */
    private String decrypt(String rawValue) {
        return rawValue == null ? null : requireCryptoService().decrypt(rawValue);
    }

    /**
     * 校验 require Crypto Service 条件。
     *
     * @return 处理结果
     */
    private DbFieldCryptoService requireCryptoService() {
        if (cryptoService == null) {
            throw new IllegalStateException("数据库字段加密服务未初始化，请确认 DbCryptoConfig 已加载");
        }
        return cryptoService;
    }
}
