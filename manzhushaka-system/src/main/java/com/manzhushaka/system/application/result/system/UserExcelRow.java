package com.manzhushaka.system.application.result.system;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;
import com.manzhushaka.common.annotation.Excel.Type;

/**
 * 用户导入导出行。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class UserExcelRow
{
    @Excel(name = "用户序号", type = Type.EXPORT, cellType = ColumnType.NUMERIC)
    private Long userId;

    @Excel(name = "部门编号", type = Type.IMPORT)
    private Long deptId;

    @Excel(name = "登录名称")
    private String userName;

    @Excel(name = "用户名称")
    private String nickName;

    @Excel(name = "用户邮箱")
    private String email;

    @Excel(name = "手机号码", cellType = ColumnType.TEXT)
    private String phonenumber;

    @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    @Excel(name = "账号状态", readConverterExp = "0=正常,1=停用")
    private String status;

    @Excel(name = "部门名称", type = Type.EXPORT)
    private String deptName;

    @Excel(name = "部门负责人", type = Type.EXPORT)
    private String deptLeader;

    @Excel(name = "最后登录IP", type = Type.EXPORT)
    private String loginIp;

    @Excel(name = "最后登录时间", dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    private Date loginDate;

    public UserExcelRow()
    {
    }

    public UserExcelRow(Long userId, Long deptId, String userName, String nickName, String email,
            String phonenumber, String sex, String status, String deptName, String deptLeader,
            String loginIp, Date loginDate)
    {
        this.userId = userId;
        this.deptId = deptId;
        this.userName = userName;
        this.nickName = nickName;
        this.email = email;
        this.phonenumber = phonenumber;
        this.sex = sex;
        this.status = status;
        this.deptName = deptName;
        this.deptLeader = deptLeader;
        this.loginIp = loginIp;
        this.loginDate = loginDate;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getSex()
    {
        return sex;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public String getDeptLeader()
    {
        return deptLeader;
    }

    public void setDeptLeader(String deptLeader)
    {
        this.deptLeader = deptLeader;
    }

    public String getLoginIp()
    {
        return loginIp;
    }

    public void setLoginIp(String loginIp)
    {
        this.loginIp = loginIp;
    }

    public Date getLoginDate()
    {
        return loginDate;
    }

    public void setLoginDate(Date loginDate)
    {
        this.loginDate = loginDate;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("userId", userId)
                .append("deptId", deptId)
                .append("userName", userName)
                .append("nickName", nickName)
                .append("sex", sex)
                .append("status", status)
                .append("deptName", deptName)
                .append("loginDate", loginDate)
                .toString();
    }
}
