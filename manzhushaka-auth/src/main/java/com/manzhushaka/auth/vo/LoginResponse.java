package com.manzhushaka.auth.vo;

import java.util.List;

/**
 * 承载 LoginResponse 响应数据。
 */
public class LoginResponse {
    private UserInfo userInfo;

    /**
     * 返回 userInfo。
     *
     * @return 字段值
     */
    public UserInfo getUserInfo() {
        return userInfo;
    }

    /**
     * 设置 userInfo。
     *
     * @param userInfo userInfo 参数
     */
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public static class UserInfo {
        private Long userId;
        private String username;
        private String nickname;
        private Long deptId;
        private String deptName;
        private List<String> roleCodes;
        private List<String> permCodes;

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
    }
}
