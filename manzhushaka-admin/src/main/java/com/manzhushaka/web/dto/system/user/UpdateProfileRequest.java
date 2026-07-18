package com.manzhushaka.web.dto.system.user;

/**
 * 更新个人资料请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class UpdateProfileRequest
{
    private String nickName;
    private String email;
    private String phonenumber;
    private String sex;

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
}
