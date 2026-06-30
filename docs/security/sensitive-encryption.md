# 敏感配置与敏感字段加密说明

## 配置文件加密

配置文件支持 `ENC(...)` 密文。解密密钥通过 `JASYPT_ENCRYPTOR_PASSWORD` 注入，不允许写入仓库配置文件。

示例启动参数：

```bash
export JASYPT_ENCRYPTOR_PASSWORD='change-me'
export TOKEN_SECRET='ENC(base64-ciphertext)'
```

## 字段加密

业务敏感字段使用 AES-GCM 随机 IV 加密，密文格式为 `v1:<iv>:<ciphertext>`。精确查询使用 HmacSHA256 摘要列，不支持模糊查询。

必需环境变量：

```bash
export SENSITIVE_FIELD_CRYPTO_ENABLED=true
export SENSITIVE_FIELD_AES_KEY='<base64-encoded-32-byte-key>'
export SENSITIVE_FIELD_HMAC_KEY='<base64-encoded-32-byte-key>'
```

## 密钥轮换

密文字段带版本前缀。轮换密钥时新增版本加密器，读取旧版本、写入新版本，并通过迁移任务重写历史密文。

## 迁移步骤

1. 先上线新增列和代码，保持 `SENSITIVE_FIELD_CRYPTO_ENABLED=false`，此时只允许读取历史明文，不允许新增或修改加密字段。
2. 设置密钥并开启加密。
3. 对历史 `email`、`phonenumber` 执行一次性迁移，生成密文和 hash。
4. 验证用户列表、登录、唯一性校验、用户新增和修改。
5. 清理迁移过程日志，确认无明文敏感值落库或落日志。