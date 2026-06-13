package com.manzhushaka.common.context;

import com.manzhushaka.common.enums.DataScopeType;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 定义 LoginUser。
 */
public class LoginUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private Long deptId;
    /**
     * 执行 method 逻辑。
     *
     * @return 处理结果
     */
    private List<String> roleCodes = new ArrayList<>();
    /**
     * 执行 method 逻辑。
     *
     * @return 处理结果
     */
    private List<String> permCodes = new ArrayList<>();
    /**
     * 执行 method 逻辑。
     *
     * @return 处理结果
     */
    private List<DataScopeType> dataScopes = new ArrayList<>();

    /**
     * 返回 userId。
     *
     * @return 字段值
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置 userId。
     *
     * @param userId 用户 ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 返回 username。
     *
     * @return 字段值
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置 username。
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 返回 deptId。
     *
     * @return 字段值
     */
    public Long getDeptId() {
        return deptId;
    }

    /**
     * 设置 deptId。
     *
     * @param deptId 部门 ID
     */
    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    /**
     * 返回 roleCodes。
     *
     * @return 字段值
     */
    public List<String> getRoleCodes() {
        return roleCodes;
    }

    /**
     * 设置 roleCodes。
     *
     * @param roleCodes roleCodes 参数
     */
    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }

    /**
     * 返回 permCodes。
     *
     * @return 字段值
     */
    public List<String> getPermCodes() {
        return permCodes;
    }

    /**
     * 设置 permCodes。
     *
     * @param permCodes permCodes 参数
     */
    public void setPermCodes(List<String> permCodes) {
        this.permCodes = permCodes;
    }

    /**
     * 返回 dataScopes。
     *
     * @return 字段值
     */
    public List<DataScopeType> getDataScopes() {
        return dataScopes;
    }

    /**
     * 设置 dataScopes。
     *
     * @param dataScopes dataScopes 参数
     */
    public void setDataScopes(List<DataScopeType> dataScopes) {
        this.dataScopes = dataScopes;
    }
}
