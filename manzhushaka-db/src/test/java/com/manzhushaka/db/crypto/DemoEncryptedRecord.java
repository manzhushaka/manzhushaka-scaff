package com.manzhushaka.db.crypto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "demo_encrypted_record", autoResultMap = true)
public class DemoEncryptedRecord {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String username;
    @TableField(value = "mobile_ciphertext", typeHandler = EncryptStringTypeHandler.class)
    private String mobile;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
