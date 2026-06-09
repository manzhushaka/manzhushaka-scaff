package com.manzhushaka.common.context;

import com.manzhushaka.common.enums.DataScopeType;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LoginUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private Long deptId;
    private List<String> roleCodes = new ArrayList<>();
    private List<String> permCodes = new ArrayList<>();
    private List<DataScopeType> dataScopes = new ArrayList<>();

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }

    public List<String> getPermCodes() {
        return permCodes;
    }

    public void setPermCodes(List<String> permCodes) {
        this.permCodes = permCodes;
    }

    public List<DataScopeType> getDataScopes() {
        return dataScopes;
    }

    public void setDataScopes(List<DataScopeType> dataScopes) {
        this.dataScopes = dataScopes;
    }
}
