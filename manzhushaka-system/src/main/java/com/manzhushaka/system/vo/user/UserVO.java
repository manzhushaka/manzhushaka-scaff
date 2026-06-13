package com.manzhushaka.system.vo.user;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 承载 UserVO 响应数据。
 */
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private Long deptId;
    private String deptName;
    private Integer status;
    private List<String> roleCodes;
    private LocalDateTime createTime;

    /**
     * 返回 id。
     *
     * @return 字段值
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置 id。
     *
     * @param id 主键 ID
     */
    public void setId(Long id) {
        this.id = id;
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
     * 返回 nickname。
     *
     * @return 字段值
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 设置 nickname。
     *
     * @param nickname nickname 参数
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
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
     * 返回 deptName。
     *
     * @return 字段值
     */
    public String getDeptName() {
        return deptName;
    }

    /**
     * 设置 deptName。
     *
     * @param deptName deptName 参数
     */
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    /**
     * 返回 status。
     *
     * @return 字段值
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置 status。
     *
     * @param status status 参数
     */
    public void setStatus(Integer status) {
        this.status = status;
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
     * 返回 createTime。
     *
     * @return 字段值
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置 createTime。
     *
     * @param createTime createTime 参数
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
