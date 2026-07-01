# 敏感配置与敏感字段加密实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 为启动配置中的敏感值接入密文解密能力，并为业务敏感字段提供可测试、可迁移、可查询的存储加密基础设施。

**架构：** 配置文件加密由 `jasypt-spring-boot-starter` 负责，只在应用启动读取配置时解密 `ENC(...)`。业务字段加密独立放在 `manzhushaka-common`，通过 AES-GCM 加密器、HMAC 检索摘要和 MyBatis TypeHandler 接入持久化层；试点字段先限定为 `sys_user.email` 和 `sys_user.phonenumber`，并用额外 hash 列承载精确查询。

**技术栈：** Spring Boot 4.0.6、Jasypt Spring Boot Starter 4.0.4、Spring Security Crypto、Java JCA AES/GCM/NoPadding、HmacSHA256、MyBatis XML、JUnit 5。

---

## 文件结构

- 修改：`pom.xml`
  - 新增 `jasypt-spring-boot.version` 属性，并在 `dependencyManagement` 中管理配置解密 starter 版本。
- 修改：`manzhushaka-admin/pom.xml`
  - 引入 `com.github.ulisesbocchio:jasypt-spring-boot-starter`，让启动模块具备 `ENC(...)` 配置解密能力。
- 修改：`manzhushaka-admin/src/main/resources/application.yml`
  - 添加 `jasypt.encryptor` 配置，密钥只从环境变量或启动参数读取。
  - 将 `token.secret` 的默认明文改成环境变量占位，示例密文只放注释。
- 修改：`manzhushaka-admin/src/main/resources/application-dev.yml`
  - 将数据库、Druid 控制台等敏感默认值改成环境变量占位或空默认值。
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/annotation/EncryptedField.java`
  - 标记需要存储加密的字段，支持指定 hash companion 字段名。
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/enums/EncryptedFieldType.java`
  - 标识字段类型，先支持 `GENERAL`、`EMAIL`、`PHONE`。
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/CryptoProperties.java`
  - 从 `manzhushaka.crypto` 前缀绑定字段加密配置。
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/SensitiveFieldEncryptor.java`
  - 定义字段加密、解密、hash 计算接口。
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/AesGcmSensitiveFieldEncryptor.java`
  - 实现 AES-GCM 随机 IV 可逆加密和 HmacSHA256 检索摘要。
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/SensitiveFieldCryptoHolder.java`
  - 为 MyBatis TypeHandler 提供静态委托入口，由 Spring 初始化。
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/NoopSensitiveFieldEncryptor.java`
  - 在字段加密未启用时显式拒绝写入敏感字段，避免 hash 列落入明文。
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/config/CryptoConfig.java`
  - 注册 `CryptoProperties` 和 `SensitiveFieldEncryptor`。
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/mybatis/typehandler/EncryptedStringTypeHandler.java`
  - MyBatis 字符串字段加解密 TypeHandler。
- 创建：`manzhushaka-common/src/test/java/com/manzhushaka/common/crypto/AesGcmSensitiveFieldEncryptorTest.java`
  - 验证密文格式、随机 IV、解密、hash 稳定性、非法密钥失败。
- 创建：`manzhushaka-common/src/test/java/com/manzhushaka/common/mybatis/typehandler/EncryptedStringTypeHandlerTest.java`
  - 验证 TypeHandler 写入密文、读取明文、空值透传。
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/entity/SysUser.java`
  - 为 `email`、`phonenumber` 增加 `@EncryptedField` 标记，并新增 `emailHash`、`phonenumberHash` 字段及普通访问器。
- 创建：`manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/support/SysUserSensitiveFieldSupport.java`
  - 在 service/repository 边界为用户邮箱、手机号填充检索 hash，避免在实体 setter 中加入业务逻辑。
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/mapper/SysUserMapper.java`
  - 将邮箱、手机号唯一性校验参数改成 hash 语义，并使用 `@Param` 固定 XML 参数名。
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/mapper/SysUserMapper.java`
  - 同步 legacy mapper 接口的方法签名和 Javadoc，避免双 mapper 包语义漂移。
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/SysUserServiceImpl.java`
  - 唯一性校验改为读取 `SysUser` 上已生成的 hash。
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/repository/UserRepositoryImpl.java`
  - 仓储层字符串校验入口先计算 hash，再调用 mapper。
- 修改：`manzhushaka-system/src/main/resources/mapper/system/SysUserMapper.xml`
  - 为 `email`、`phonenumber` result 和参数绑定接入 `EncryptedStringTypeHandler`。
  - `checkEmailUnique`、`checkPhoneUnique` 改为使用 hash 精确匹配。
  - 列表手机号查询第一阶段降级为精确查询或后端明确拒绝模糊查询，本计划选择精确查询。
- 创建：`manzhushaka-system/src/test/java/com/manzhushaka/system/infrastructure/persistence/support/SysUserSensitiveFieldSupportTest.java`
  - 验证用户邮箱、手机号 hash 由 support 类统一填充。
- 修改：`sql/manzhushaka_db_init.sql`
  - 为 `sys_user` 增加 `email_hash`、`phonenumber_hash` 字段和普通索引。
  - 保留原 `email`、`phonenumber` 列名，长度扩大到能容纳密文。
- 创建：`docs/security/sensitive-encryption.md`
  - 记录配置密文生成、字段加密密钥注入、密钥轮换、查询限制和迁移步骤。

## 明确边界

- 不把用户登录密码改成可逆加密。`PasswordUtils` 继续使用 BCrypt。
- 不加密所有业务字段，只先覆盖 `sys_user.email` 和 `sys_user.phonenumber`。
- 不支持手机号、邮箱密文字段的模糊查询。第一阶段只支持精确匹配和唯一性校验。
- 不在配置文件中写入真实解密密钥、字段加密密钥或 HMAC 密钥。
- 不改变前端字段名。接口仍使用 `email`、`phonenumber`。
- 字段加密上线后，写入 `sys_user.email` 和 `sys_user.phonenumber` 前必须启用 `manzhushaka.crypto.enabled=true` 并提供密钥；未启用时允许读取历史明文，但拒绝新增或修改这两个字段。
- 不在实体 getter/setter 中加入加密、解密或 hash 生成逻辑。实体只保存字段，hash 填充放在 service/repository 边界。

---

### 任务 1：接入配置文件密文解密依赖

**文件：**
- 修改：`pom.xml`
- 修改：`manzhushaka-admin/pom.xml`

- [ ] **步骤 1：编写依赖缺失验证**

运行：

```bash
rg -n "jasypt-spring-boot-starter|jasypt-spring-boot.version" pom.xml manzhushaka-admin/pom.xml
```

预期：没有输出，说明当前没有直接接入配置解密 starter。

- [ ] **步骤 2：在父 POM 添加版本属性和依赖管理**

在 `pom.xml` 的 `<properties>` 中加入：

```xml
<jasypt-spring-boot.version>4.0.4</jasypt-spring-boot.version>
```

在 `pom.xml` 的 `<dependencyManagement><dependencies>` 中加入：

```xml
<!-- 配置文件敏感信息密文解密 -->
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>${jasypt-spring-boot.version}</version>
</dependency>
```

- [ ] **步骤 3：在 admin 启动模块引入 starter**

在 `manzhushaka-admin/pom.xml` 的 `<dependencies>` 中加入：

```xml
<!-- 配置文件敏感信息密文解密 -->
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
</dependency>
```

- [ ] **步骤 4：验证依赖声明存在**

运行：

```bash
rg -n "jasypt-spring-boot-starter|jasypt-spring-boot.version" pom.xml manzhushaka-admin/pom.xml
```

预期：能看到父 POM 的版本属性、dependencyManagement 和 admin 模块依赖。

- [ ] **步骤 5：离线解析依赖**

运行：

```bash
mvn -o -pl manzhushaka-admin -am dependency:tree -Dincludes=com.github.ulisesbocchio:*,org.jasypt:*
```

预期：输出包含 `com.github.ulisesbocchio:jasypt-spring-boot-starter:jar:4.0.4`、`com.github.ulisesbocchio:jasypt-spring-boot:jar:4.0.4` 和 `org.jasypt:jasypt:jar:1.9.3`。如果本地 Maven 因公司仓库离线解析失败，记录失败原因，并用本机 `~/.m2/repository` 中已有 jar 作为辅助证据。

- [ ] **步骤 6：Commit**

```bash
git add pom.xml manzhushaka-admin/pom.xml
git commit -m "feat: 接入配置文件密文解密依赖"
```

---

### 任务 2：调整配置文件敏感值写法

**文件：**
- 修改：`manzhushaka-admin/src/main/resources/application.yml`
- 修改：`manzhushaka-admin/src/main/resources/application-dev.yml`

- [ ] **步骤 1：记录当前敏感配置**

运行：

```bash
rg -n "password:|secret:|username:|login-password|JDBC_|TOKEN_|REDIS_" manzhushaka-admin/src/main/resources/application*.yml
```

预期：能看到 `token.secret`、Redis 密码、JDBC 用户名密码、Druid 控制台账号密码。

- [ ] **步骤 2：配置 Jasypt 密钥来源**

在 `manzhushaka-admin/src/main/resources/application.yml` 中新增：

```yaml
# 配置文件敏感信息密文解密
jasypt:
  encryptor:
    password: ${JASYPT_ENCRYPTOR_PASSWORD:}
    algorithm: PBEWITHHMACSHA512ANDAES_256
    iv-generator-classname: org.jasypt.iv.RandomIvGenerator
```

- [ ] **步骤 3：收敛 token secret 明文默认值**

将 `token.secret` 改为：

```yaml
token:
  # 令牌自定义标识
  header: Authorization
  # 令牌密钥，生产环境必须通过 TOKEN_SECRET 或 ENC(...) 注入
  secret: ${TOKEN_SECRET:}
  # 令牌有效期（默认30分钟）
  expireTime: 30
```

- [ ] **步骤 4：收敛开发库密码默认值**

将 `manzhushaka-admin/src/main/resources/application-dev.yml` 中主库配置改为：

```yaml
master:
    url: ${JDBC_MASTER_URL:jdbc:mysql://localhost:3306/manzhushaka-scaff?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8}
    username: ${JDBC_MASTER_USERNAME:root}
    password: ${JDBC_MASTER_PASSWORD:}
```

保留 slave 和 Druid 控制台密码的环境变量占位，不写真实默认值。

- [ ] **步骤 5：验证不再包含已知明文密码**

运行：

```bash
rg -n "1a2s3d4f|abcdefghijklmnopqrstuvwxyz" manzhushaka-admin/src/main/resources/application*.yml
```

预期：没有输出。

- [ ] **步骤 6：验证配置格式**

运行：

```bash
mvn -pl manzhushaka-admin -am test -DskipTests
```

预期：Maven 编译阶段通过。如果公司 Maven 仓库连接失败，记录具体仓库和 artifact 错误。

- [ ] **步骤 7：Commit**

```bash
git add manzhushaka-admin/src/main/resources/application.yml manzhushaka-admin/src/main/resources/application-dev.yml
git commit -m "chore: 收敛敏感配置明文默认值"
```

---

### 任务 3：实现字段加密核心能力

**文件：**
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/enums/EncryptedFieldType.java`
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/annotation/EncryptedField.java`
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/CryptoProperties.java`
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/SensitiveFieldEncryptor.java`
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/AesGcmSensitiveFieldEncryptor.java`
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/config/CryptoConfig.java`
- 测试：`manzhushaka-common/src/test/java/com/manzhushaka/common/crypto/AesGcmSensitiveFieldEncryptorTest.java`

- [ ] **步骤 1：编写失败测试**

创建 `manzhushaka-common/src/test/java/com/manzhushaka/common/crypto/AesGcmSensitiveFieldEncryptorTest.java`：

```java
package com.manzhushaka.common.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class AesGcmSensitiveFieldEncryptorTest {

    private static final String AES_KEY = Base64.getEncoder()
            .encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));
    private static final String HMAC_KEY = Base64.getEncoder()
            .encodeToString("abcdef0123456789abcdef0123456789".getBytes(StandardCharsets.UTF_8));

    @Test
    void shouldEncryptAndDecryptValue() {
        CryptoProperties properties = new CryptoProperties();
        properties.setAesKey(AES_KEY);
        properties.setHmacKey(HMAC_KEY);
        AesGcmSensitiveFieldEncryptor encryptor = new AesGcmSensitiveFieldEncryptor(properties);

        String ciphertext = encryptor.encrypt("13800138000");

        assertTrue(ciphertext.startsWith("v1:"));
        assertEquals("13800138000", encryptor.decrypt(ciphertext));
    }

    @Test
    void shouldUseRandomIvForSamePlainText() {
        CryptoProperties properties = new CryptoProperties();
        properties.setAesKey(AES_KEY);
        properties.setHmacKey(HMAC_KEY);
        AesGcmSensitiveFieldEncryptor encryptor = new AesGcmSensitiveFieldEncryptor(properties);

        String first = encryptor.encrypt("user@example.com");
        String second = encryptor.encrypt("user@example.com");

        assertNotEquals(first, second);
        assertEquals("user@example.com", encryptor.decrypt(first));
        assertEquals("user@example.com", encryptor.decrypt(second));
    }

    @Test
    void shouldGenerateStableHash() {
        CryptoProperties properties = new CryptoProperties();
        properties.setAesKey(AES_KEY);
        properties.setHmacKey(HMAC_KEY);
        AesGcmSensitiveFieldEncryptor encryptor = new AesGcmSensitiveFieldEncryptor(properties);

        assertEquals(encryptor.hash("user@example.com"), encryptor.hash("user@example.com"));
        assertNotEquals(encryptor.hash("user@example.com"), encryptor.hash("other@example.com"));
    }

    @Test
    void shouldRejectBlankKey() {
        CryptoProperties properties = new CryptoProperties();
        properties.setAesKey("");
        properties.setHmacKey(HMAC_KEY);

        assertThrows(IllegalArgumentException.class, () -> new AesGcmSensitiveFieldEncryptor(properties));
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：

```bash
mvn -pl manzhushaka-common -Dtest=AesGcmSensitiveFieldEncryptorTest test
```

预期：FAIL，编译错误包含 `cannot find symbol`，因为加密类尚未创建。

- [ ] **步骤 3：创建字段类型枚举**

创建 `manzhushaka-common/src/main/java/com/manzhushaka/common/enums/EncryptedFieldType.java`：

```java
package com.manzhushaka.common.enums;

/**
 * 存储加密字段类型。
 *
 * @author manzhushaka
 */
public enum EncryptedFieldType {
    /** 通用文本 */
    GENERAL,

    /** 邮箱 */
    EMAIL,

    /** 手机号 */
    PHONE
}
```

- [ ] **步骤 4：创建字段加密注解**

创建 `manzhushaka-common/src/main/java/com/manzhushaka/common/annotation/EncryptedField.java`：

```java
package com.manzhushaka.common.annotation;

import com.manzhushaka.common.enums.EncryptedFieldType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 存储加密字段标记。
 *
 * @author manzhushaka
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EncryptedField {

    /**
     * 字段类型。
     *
     * @return 字段类型
     */
    EncryptedFieldType type() default EncryptedFieldType.GENERAL;

    /**
     * 精确查询使用的摘要字段名。
     *
     * @return 摘要字段名
     */
    String hashField() default "";
}
```

- [ ] **步骤 5：创建配置属性类**

创建 `manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/CryptoProperties.java`：

```java
package com.manzhushaka.common.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 敏感字段加密配置。
 *
 * @author manzhushaka
 */
@ConfigurationProperties(prefix = "manzhushaka.crypto")
public class CryptoProperties {

    /** 是否启用敏感字段加密 */
    private boolean enabled = false;

    /** Base64 编码的 AES 密钥 */
    private String aesKey;

    /** Base64 编码的 HMAC 密钥 */
    private String hmacKey;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public String getHmacKey() {
        return hmacKey;
    }

    public void setHmacKey(String hmacKey) {
        this.hmacKey = hmacKey;
    }
}
```

- [ ] **步骤 6：创建加密接口**

创建 `manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/SensitiveFieldEncryptor.java`：

```java
package com.manzhushaka.common.crypto;

/**
 * 敏感字段加密器。
 *
 * @author manzhushaka
 */
public interface SensitiveFieldEncryptor {

    /**
     * 加密明文。
     *
     * @param plaintext 明文
     * @return 密文
     */
    String encrypt(String plaintext);

    /**
     * 解密密文。
     *
     * @param ciphertext 密文
     * @return 明文
     */
    String decrypt(String ciphertext);

    /**
     * 计算精确查询摘要。
     *
     * @param plaintext 明文
     * @return 摘要
     */
    String hash(String plaintext);

    /**
     * 判断值是否已加密。
     *
     * @param value 待判断值
     * @return 是否密文
     */
    boolean isCiphertext(String value);
}
```

- [ ] **步骤 7：实现 AES-GCM 加密器**

创建 `manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/AesGcmSensitiveFieldEncryptor.java`：

```java
package com.manzhushaka.common.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.StringUtils;

/**
 * AES-GCM 敏感字段加密器。
 *
 * @author manzhushaka
 */
public class AesGcmSensitiveFieldEncryptor implements SensitiveFieldEncryptor {

    private static final String VERSION_PREFIX = "v1:";
    private static final String AES_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKeySpec aesKeySpec;
    private final SecretKeySpec hmacKeySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmSensitiveFieldEncryptor(CryptoProperties properties) {
        byte[] aesKey = decodeKey(properties.getAesKey(), "AES");
        byte[] hmacKey = decodeKey(properties.getHmacKey(), "HMAC");
        this.aesKeySpec = new SecretKeySpec(aesKey, AES_ALGORITHM);
        this.hmacKeySpec = new SecretKeySpec(hmacKey, HMAC_ALGORITHM);
    }

    @Override
    public String encrypt(String plaintext) {
        if (StringUtils.isBlank(plaintext) || isCiphertext(plaintext)) {
            return plaintext;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return VERSION_PREFIX + Base64.getEncoder().encodeToString(iv) + ":"
                    + Base64.getEncoder().encodeToString(encrypted);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Encrypt sensitive field failed", e);
        }
    }

    @Override
    public String decrypt(String ciphertext) {
        if (StringUtils.isBlank(ciphertext) || !isCiphertext(ciphertext)) {
            return ciphertext;
        }
        String[] parts = ciphertext.split(":", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid sensitive field ciphertext format");
        }
        try {
            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] encrypted = Base64.getDecoder().decode(parts[2]);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, aesKeySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Decrypt sensitive field failed", e);
        }
    }

    @Override
    public String hash(String plaintext) {
        if (StringUtils.isBlank(plaintext)) {
            return plaintext;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(hmacKeySpec);
            byte[] digest = mac.doFinal(plaintext.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Hash sensitive field failed", e);
        }
    }

    @Override
    public boolean isCiphertext(String value) {
        return StringUtils.startsWith(value, VERSION_PREFIX);
    }

    private byte[] decodeKey(String key, String keyName) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException(keyName + " key must not be blank");
        }
        byte[] decoded = Base64.getDecoder().decode(key);
        if (decoded.length < 32) {
            throw new IllegalArgumentException(keyName + " key must be at least 256 bits");
        }
        return decoded;
    }
}
```

- [ ] **步骤 8：注册 Spring Bean**

创建 `manzhushaka-common/src/main/java/com/manzhushaka/common/config/CryptoConfig.java`：

```java
package com.manzhushaka.common.config;

import com.manzhushaka.common.crypto.AesGcmSensitiveFieldEncryptor;
import com.manzhushaka.common.crypto.CryptoProperties;
import com.manzhushaka.common.crypto.SensitiveFieldEncryptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 敏感字段加密配置。
 *
 * @author manzhushaka
 */
@Configuration
@EnableConfigurationProperties(CryptoProperties.class)
public class CryptoConfig {

    /**
     * 创建敏感字段加密器。
     *
     * @param properties 加密配置
     * @return 敏感字段加密器
     */
    @Bean
    @ConditionalOnProperty(prefix = "manzhushaka.crypto", name = "enabled", havingValue = "true")
    public SensitiveFieldEncryptor sensitiveFieldEncryptor(CryptoProperties properties) {
        return new AesGcmSensitiveFieldEncryptor(properties);
    }
}
```

- [ ] **步骤 9：运行测试验证通过**

运行：

```bash
mvn -pl manzhushaka-common -Dtest=AesGcmSensitiveFieldEncryptorTest test
```

预期：`Tests run: 4, Failures: 0, Errors: 0`。

- [ ] **步骤 10：Commit**

```bash
git add manzhushaka-common/src/main/java/com/manzhushaka/common/enums/EncryptedFieldType.java \
  manzhushaka-common/src/main/java/com/manzhushaka/common/annotation/EncryptedField.java \
  manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/CryptoProperties.java \
  manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/SensitiveFieldEncryptor.java \
  manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/AesGcmSensitiveFieldEncryptor.java \
  manzhushaka-common/src/main/java/com/manzhushaka/common/config/CryptoConfig.java \
  manzhushaka-common/src/test/java/com/manzhushaka/common/crypto/AesGcmSensitiveFieldEncryptorTest.java
git commit -m "feat: 增加敏感字段加密核心能力"
```

---

### 任务 4：实现 MyBatis 字段加解密 TypeHandler

**文件：**
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/SensitiveFieldCryptoHolder.java`
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/NoopSensitiveFieldEncryptor.java`
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/mybatis/typehandler/EncryptedStringTypeHandler.java`
- 测试：`manzhushaka-common/src/test/java/com/manzhushaka/common/mybatis/typehandler/EncryptedStringTypeHandlerTest.java`

- [ ] **步骤 1：编写失败测试**

创建 `manzhushaka-common/src/test/java/com/manzhushaka/common/mybatis/typehandler/EncryptedStringTypeHandlerTest.java`：

```java
package com.manzhushaka.common.mybatis.typehandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.manzhushaka.common.crypto.SensitiveFieldCryptoHolder;
import com.manzhushaka.common.crypto.SensitiveFieldEncryptor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EncryptedStringTypeHandlerTest {

    @AfterEach
    void tearDown() {
        SensitiveFieldCryptoHolder.clear();
    }

    @Test
    void shouldEncryptParameter() throws Exception {
        SensitiveFieldEncryptor encryptor = mock(SensitiveFieldEncryptor.class);
        when(encryptor.encrypt("plain")).thenReturn("v1:cipher");
        SensitiveFieldCryptoHolder.setEncryptor(encryptor);
        PreparedStatement statement = mock(PreparedStatement.class);
        EncryptedStringTypeHandler handler = new EncryptedStringTypeHandler();

        handler.setNonNullParameter(statement, 1, "plain", null);

        verify(statement).setString(1, "v1:cipher");
    }

    @Test
    void shouldDecryptResult() throws Exception {
        SensitiveFieldEncryptor encryptor = mock(SensitiveFieldEncryptor.class);
        when(encryptor.decrypt("v1:cipher")).thenReturn("plain");
        SensitiveFieldCryptoHolder.setEncryptor(encryptor);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("email")).thenReturn("v1:cipher");
        EncryptedStringTypeHandler handler = new EncryptedStringTypeHandler();

        assertEquals("plain", handler.getNullableResult(resultSet, "email"));
    }

    @Test
    void shouldReadPlainResultWhenEncryptorMissing() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("email")).thenReturn("plain");
        EncryptedStringTypeHandler handler = new EncryptedStringTypeHandler();

        assertEquals("plain", handler.getNullableResult(resultSet, "email"));
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：

```bash
mvn -pl manzhushaka-common -Dtest=EncryptedStringTypeHandlerTest test
```

预期：FAIL，编译错误包含 `SensitiveFieldCryptoHolder` 或 `EncryptedStringTypeHandler` 不存在。

- [ ] **步骤 3：实现静态委托 Holder**

创建 `manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/SensitiveFieldCryptoHolder.java`：

```java
package com.manzhushaka.common.crypto;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 敏感字段加密器静态访问入口。
 *
 * @author manzhushaka
 */
@Component
public class SensitiveFieldCryptoHolder implements InitializingBean {

    private static SensitiveFieldEncryptor encryptor;

    private final SensitiveFieldEncryptor injectedEncryptor;

    public SensitiveFieldCryptoHolder(@Nullable SensitiveFieldEncryptor injectedEncryptor) {
        this.injectedEncryptor = injectedEncryptor;
    }

    @Override
    public void afterPropertiesSet() {
        encryptor = injectedEncryptor;
    }

    public static void setEncryptor(SensitiveFieldEncryptor sensitiveFieldEncryptor) {
        encryptor = sensitiveFieldEncryptor;
    }

    public static void clear() {
        encryptor = null;
    }

    public static String encrypt(String plaintext) {
        return activeEncryptor().encrypt(plaintext);
    }

    public static String decrypt(String ciphertext) {
        return activeEncryptor().decrypt(ciphertext);
    }

    public static String hash(String plaintext) {
        return activeEncryptor().hash(plaintext);
    }

    private static SensitiveFieldEncryptor activeEncryptor() {
        return encryptor == null ? NoopSensitiveFieldEncryptor.INSTANCE : encryptor;
    }
}
```

- [ ] **步骤 4：实现未启用加密时的显式策略**

创建 `manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/NoopSensitiveFieldEncryptor.java`：

```java
package com.manzhushaka.common.crypto;

import org.apache.commons.lang3.StringUtils;

/**
 * 未启用敏感字段加密时的保护性加密器。
 *
 * @author manzhushaka
 */
public enum NoopSensitiveFieldEncryptor implements SensitiveFieldEncryptor {
    /** 单例 */
    INSTANCE;

    @Override
    public String encrypt(String plaintext) {
        if (StringUtils.isBlank(plaintext) || isCiphertext(plaintext)) {
            return plaintext;
        }
        throw new IllegalStateException("Sensitive field crypto is disabled");
    }

    @Override
    public String decrypt(String ciphertext) {
        return ciphertext;
    }

    @Override
    public String hash(String plaintext) {
        if (StringUtils.isBlank(plaintext)) {
            return plaintext;
        }
        return null;
    }

    @Override
    public boolean isCiphertext(String value) {
        return StringUtils.startsWith(value, "v1:");
    }
}
```

- [ ] **步骤 5：实现 TypeHandler**

创建 `manzhushaka-common/src/main/java/com/manzhushaka/common/mybatis/typehandler/EncryptedStringTypeHandler.java`：

```java
package com.manzhushaka.common.mybatis.typehandler;

import com.manzhushaka.common.crypto.SensitiveFieldCryptoHolder;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * 加密字符串 TypeHandler。
 *
 * @author manzhushaka
 */
public class EncryptedStringTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, SensitiveFieldCryptoHolder.encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return SensitiveFieldCryptoHolder.decrypt(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return SensitiveFieldCryptoHolder.decrypt(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return SensitiveFieldCryptoHolder.decrypt(cs.getString(columnIndex));
    }
}
```

- [ ] **步骤 6：调整空加密器测试预期**

把 `EncryptedStringTypeHandlerTest.shouldReadPlainResultWhenEncryptorMissing` 保持为读取场景，只验证 `getNullableResult` 明文透传。新增写入保护测试：

```java
@Test
void shouldRejectPlainParameterWhenEncryptorMissing() {
    PreparedStatement statement = mock(PreparedStatement.class);
    EncryptedStringTypeHandler handler = new EncryptedStringTypeHandler();

    assertThrows(IllegalStateException.class,
            () -> handler.setNonNullParameter(statement, 1, "plain", null));
}
```

并补充 import：

```java
import static org.junit.jupiter.api.Assertions.assertThrows;
```

新增 hash 行为测试，确保读取历史明文经过实体 setter 时不会因为缺少加密器抛异常：

```java
@Test
void shouldReturnNullHashWhenEncryptorMissing() {
    assertNull(SensitiveFieldCryptoHolder.hash("plain"));
}
```

并补充 import：

```java
import static org.junit.jupiter.api.Assertions.assertNull;
```

- [ ] **步骤 7：运行测试验证通过**

运行：

```bash
mvn -pl manzhushaka-common -Dtest=EncryptedStringTypeHandlerTest test
```

预期：`Tests run: 5, Failures: 0, Errors: 0`。

- [ ] **步骤 8：Commit**

```bash
git add manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/SensitiveFieldCryptoHolder.java \
  manzhushaka-common/src/main/java/com/manzhushaka/common/crypto/NoopSensitiveFieldEncryptor.java \
  manzhushaka-common/src/main/java/com/manzhushaka/common/mybatis/typehandler/EncryptedStringTypeHandler.java \
  manzhushaka-common/src/test/java/com/manzhushaka/common/mybatis/typehandler/EncryptedStringTypeHandlerTest.java
git commit -m "feat: 增加 MyBatis 敏感字段加解密处理器"
```

---

### 任务 5：用户邮箱和手机号接入加密与 hash 查询

**文件：**
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/entity/SysUser.java`
- 创建：`manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/support/SysUserSensitiveFieldSupport.java`
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/mapper/SysUserMapper.java`
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/mapper/SysUserMapper.java`
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/SysUserServiceImpl.java`
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/repository/UserRepositoryImpl.java`
- 修改：`manzhushaka-system/src/main/resources/mapper/system/SysUserMapper.xml`
- 测试：`manzhushaka-system/src/test/java/com/manzhushaka/system/infrastructure/persistence/support/SysUserSensitiveFieldSupportTest.java`

- [ ] **步骤 1：编写失败测试**

创建 `manzhushaka-system/src/test/java/com/manzhushaka/system/infrastructure/persistence/support/SysUserSensitiveFieldSupportTest.java`：

```java
package com.manzhushaka.system.infrastructure.persistence.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.manzhushaka.common.crypto.SensitiveFieldCryptoHolder;
import com.manzhushaka.common.crypto.SensitiveFieldEncryptor;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SysUserSensitiveFieldSupportTest {

    @AfterEach
    void tearDown() {
        SensitiveFieldCryptoHolder.clear();
    }

    @Test
    void shouldPopulateEmailHash() {
        SensitiveFieldCryptoHolder.setEncryptor(new StubEncryptor());
        SysUser user = new SysUser();
        user.setEmail("user@example.com");

        SysUserSensitiveFieldSupport.fillHashes(user);

        assertEquals("user@example.com", user.getEmail());
        assertEquals("hash:user@example.com", user.getEmailHash());
    }

    @Test
    void shouldPopulatePhoneHash() {
        SensitiveFieldCryptoHolder.setEncryptor(new StubEncryptor());
        SysUser user = new SysUser();
        user.setPhonenumber("13800138000");

        SysUserSensitiveFieldSupport.fillHashes(user);

        assertEquals("13800138000", user.getPhonenumber());
        assertEquals("hash:13800138000", user.getPhonenumberHash());
    }

    private static class StubEncryptor implements SensitiveFieldEncryptor {
        @Override
        public String encrypt(String plaintext) {
            return "v1:" + plaintext;
        }

        @Override
        public String decrypt(String ciphertext) {
            return ciphertext;
        }

        @Override
        public String hash(String plaintext) {
            return "hash:" + plaintext;
        }

        @Override
        public boolean isCiphertext(String value) {
            return value != null && value.startsWith("v1:");
        }
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：

```bash
mvn -pl manzhushaka-system -Dtest=SysUserSensitiveFieldSupportTest test
```

预期：FAIL，编译错误包含 `SysUserSensitiveFieldSupport`、`getEmailHash` 或 `getPhonenumberHash` 不存在。

- [ ] **步骤 3：修改 SysUser 字段**

在 `SysUser` 中新增 import：

```java
import com.manzhushaka.common.annotation.EncryptedField;
import com.manzhushaka.common.enums.EncryptedFieldType;
```

将字段改为：

```java
/** 用户邮箱 */
@Excel(name = "用户邮箱")
@EncryptedField(type = EncryptedFieldType.EMAIL, hashField = "emailHash")
private String email;

/** 用户邮箱检索摘要 */
private String emailHash;

/** 手机号码 */
@Excel(name = "手机号码", cellType = ColumnType.TEXT)
@EncryptedField(type = EncryptedFieldType.PHONE, hashField = "phonenumberHash")
private String phonenumber;

/** 手机号码检索摘要 */
private String phonenumberHash;
```

将 setter 保持为普通赋值，禁止在 setter 中生成 hash：

```java
public void setEmail(String email)
{
    this.email = email;
}
```

```java
public void setPhonenumber(String phonenumber)
{
    this.phonenumber = phonenumber;
}
```

新增 getter/setter：

```java
public String getEmailHash()
{
    return emailHash;
}

public void setEmailHash(String emailHash)
{
    this.emailHash = emailHash;
}

public String getPhonenumberHash()
{
    return phonenumberHash;
}

public void setPhonenumberHash(String phonenumberHash)
{
    this.phonenumberHash = phonenumberHash;
}
```

更新 `toString()`，不要输出 `emailHash`、`phonenumberHash`。

- [ ] **步骤 4：创建 SysUser hash 填充支持类**

创建 `manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/support/SysUserSensitiveFieldSupport.java`：

```java
package com.manzhushaka.system.infrastructure.persistence.support;

import com.manzhushaka.common.crypto.SensitiveFieldCryptoHolder;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;

/**
 * 用户敏感字段检索摘要处理。
 *
 * @author manzhushaka
 */
public final class SysUserSensitiveFieldSupport {

    /**
     * 填充用户敏感字段检索摘要。
     *
     * @param user 用户对象
     */
    public static void fillHashes(SysUser user)
    {
        if (user == null)
        {
            return;
        }
        if (StringUtils.isNotEmpty(user.getEmail()))
        {
            user.setEmailHash(SensitiveFieldCryptoHolder.hash(user.getEmail()));
        }
        if (StringUtils.isNotEmpty(user.getPhonenumber()))
        {
            user.setPhonenumberHash(SensitiveFieldCryptoHolder.hash(user.getPhonenumber()));
        }
    }

    private SysUserSensitiveFieldSupport()
    {
        // 工具类，防止实例化
    }
}
```

- [ ] **步骤 5：修改 SysUserMapper resultMap**

在 `SysUserResult` 中将 email、phonenumber 映射改为：

```xml
<result property="email" column="email"
        typeHandler="com.manzhushaka.common.mybatis.typehandler.EncryptedStringTypeHandler" />
<result property="emailHash" column="email_hash" />
<result property="phonenumber" column="phonenumber"
        typeHandler="com.manzhushaka.common.mybatis.typehandler.EncryptedStringTypeHandler" />
<result property="phonenumberHash" column="phonenumber_hash" />
```

- [ ] **步骤 6：修改查询列**

把所有查询中 `u.email` 后补 `u.email_hash`，`u.phonenumber` 后补 `u.phonenumber_hash`。例如 `selectUserVo` 改为：

```xml
select u.user_id, u.dept_id, u.user_name, u.nick_name, u.email, u.email_hash, u.avatar,
u.phonenumber, u.phonenumber_hash, u.password, u.sex, u.status, u.del_flag,
u.login_ip, u.login_date, u.pwd_update_date, u.create_by, u.create_time,
u.update_by, u.update_time, u.remark,
```

列表查询也同步补充 `u.email_hash` 和 `u.phonenumber_hash`。

- [ ] **步骤 7：修改插入和更新绑定**

在 `insertUser` 字段列表中，email 后加入：

```xml
<if test="emailHash != null and emailHash != ''">email_hash,</if>
```

phonenumber 后加入：

```xml
<if test="phonenumberHash != null and phonenumberHash != ''">phonenumber_hash,</if>
```

在 values 中，email 和 phonenumber 绑定改为：

```xml
<if test="email != null and email != ''">#{email,typeHandler=com.manzhushaka.common.mybatis.typehandler.EncryptedStringTypeHandler},</if>
<if test="emailHash != null and emailHash != ''">#{emailHash},</if>
```

```xml
<if test="phonenumber != null and phonenumber != ''">#{phonenumber,typeHandler=com.manzhushaka.common.mybatis.typehandler.EncryptedStringTypeHandler},</if>
<if test="phonenumberHash != null and phonenumberHash != ''">#{phonenumberHash},</if>
```

在 `updateUser` 中改为：

```xml
<if test="email != null ">email = #{email,typeHandler=com.manzhushaka.common.mybatis.typehandler.EncryptedStringTypeHandler}, email_hash = #{emailHash},</if>
<if test="phonenumber != null ">phonenumber = #{phonenumber,typeHandler=com.manzhushaka.common.mybatis.typehandler.EncryptedStringTypeHandler}, phonenumber_hash = #{phonenumberHash},</if>
```

- [ ] **步骤 8：修改 mapper 接口参数语义**

修改 `manzhushaka-system/src/main/java/com/manzhushaka/system/mapper/SysUserMapper.java` 和 `manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/mapper/SysUserMapper.java`：

```java
/**
 * 校验手机号码是否唯一
 *
 * @param phonenumberHash 手机号码检索摘要
 * @return 结果
 */
public SysUser checkPhoneUnique(@Param("phonenumberHash") String phonenumberHash);

/**
 * 校验email是否唯一
 *
 * @param emailHash 用户邮箱检索摘要
 * @return 结果
 */
public SysUser checkEmailUnique(@Param("emailHash") String emailHash);
```

- [ ] **步骤 9：修改 XML 精确查询**

将唯一性校验改为：

```xml
<select id="checkPhoneUnique" parameterType="String" resultMap="SysUserResult">
    select user_id, phonenumber, phonenumber_hash from sys_user
    where phonenumber_hash = #{phonenumberHash} and del_flag = '0' limit 1
</select>
```

```xml
<select id="checkEmailUnique" parameterType="String" resultMap="SysUserResult">
    select user_id, email, email_hash from sys_user
    where email_hash = #{emailHash} and del_flag = '0' limit 1
</select>
```

- [ ] **步骤 10：修改 service 和 repository 调用点**

在 `manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/SysUserServiceImpl.java` 中新增 import：

```java
import com.manzhushaka.common.crypto.SensitiveFieldCryptoHolder;
import com.manzhushaka.system.infrastructure.persistence.support.SysUserSensitiveFieldSupport;
```

在 `selectUserList`、`selectAllocatedList`、`selectUnallocatedList`、`insertUser`、`registerUser`、`updateUser` 进入 mapper 前调用：

```java
SysUserSensitiveFieldSupport.fillHashes(user);
```

将唯一性校验改为：

```java
public boolean checkPhoneUnique(SysUser user)
{
    Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
    SysUserSensitiveFieldSupport.fillHashes(user);
    String phonenumberHash = user.getPhonenumberHash();
    if (StringUtils.isEmpty(phonenumberHash) && StringUtils.isNotEmpty(user.getPhonenumber()))
    {
        phonenumberHash = SensitiveFieldCryptoHolder.hash(user.getPhonenumber());
    }
    SysUser info = userMapper.checkPhoneUnique(phonenumberHash);
    if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
    {
        return UserConstants.NOT_UNIQUE;
    }
    return UserConstants.UNIQUE;
}
```

```java
public boolean checkEmailUnique(SysUser user)
{
    Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
    SysUserSensitiveFieldSupport.fillHashes(user);
    String emailHash = user.getEmailHash();
    if (StringUtils.isEmpty(emailHash) && StringUtils.isNotEmpty(user.getEmail()))
    {
        emailHash = SensitiveFieldCryptoHolder.hash(user.getEmail());
    }
    SysUser info = userMapper.checkEmailUnique(emailHash);
    if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
    {
        return UserConstants.NOT_UNIQUE;
    }
    return UserConstants.UNIQUE;
}
```

在 `manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/repository/UserRepositoryImpl.java` 中新增 import：

```java
import com.manzhushaka.common.crypto.SensitiveFieldCryptoHolder;
import com.manzhushaka.system.infrastructure.persistence.support.SysUserSensitiveFieldSupport;
```

在 `selectUserList`、`selectAllocatedList`、`selectUnallocatedList`、`insertUser`、`updateUser` 进入 mapper 前调用：

```java
SysUserSensitiveFieldSupport.fillHashes(user);
```

将字符串入口改为：

```java
public SysUser checkPhoneUnique(String phonenumber)
{
    return userMapper.checkPhoneUnique(SensitiveFieldCryptoHolder.hash(phonenumber));
}
```

```java
public SysUser checkEmailUnique(String email)
{
    return userMapper.checkEmailUnique(SensitiveFieldCryptoHolder.hash(email));
}
```

将对象入口改为：

```java
public boolean checkPhoneUnique(SysUser user)
{
    SysUserSensitiveFieldSupport.fillHashes(user);
    String phonenumberHash = user.getPhonenumberHash();
    if (StringUtils.isEmpty(phonenumberHash) && StringUtils.isNotEmpty(user.getPhonenumber()))
    {
        phonenumberHash = SensitiveFieldCryptoHolder.hash(user.getPhonenumber());
    }
    SysUser sysUser = userMapper.checkPhoneUnique(phonenumberHash);
    return sysUser == null || sysUser.getUserId().equals(user.getUserId());
}
```

```java
public boolean checkEmailUnique(SysUser user)
{
    SysUserSensitiveFieldSupport.fillHashes(user);
    String emailHash = user.getEmailHash();
    if (StringUtils.isEmpty(emailHash) && StringUtils.isNotEmpty(user.getEmail()))
    {
        emailHash = SensitiveFieldCryptoHolder.hash(user.getEmail());
    }
    SysUser sysUser = userMapper.checkEmailUnique(emailHash);
    return sysUser == null || sysUser.getUserId().equals(user.getUserId());
}
```

如果 `UserRepositoryImpl` 尚未导入 `StringUtils`，补充：

```java
import com.manzhushaka.common.utils.StringUtils;
```

- [ ] **步骤 11：列表手机号查询改成精确 hash 匹配**

把 `selectUserList`、`selectAllocatedList`、`selectUnallocatedList` 中：

```xml
AND u.phonenumber like concat('%', #{phonenumber}, '%')
```

改为：

```xml
AND u.phonenumber_hash = #{phonenumberHash}
```

确保进入 mapper 前 `SysUser.setPhonenumber(...)` 已经生成 `phonenumberHash`。如果某个调用点直接设置 hash，保持使用 `setPhonenumber` 统一生成。

- [ ] **步骤 12：运行 support 测试**

运行：

```bash
mvn -pl manzhushaka-system -Dtest=SysUserSensitiveFieldSupportTest test
```

预期：`Tests run: 2, Failures: 0, Errors: 0`。

- [ ] **步骤 13：运行 XML 关键字检查**

运行：

```bash
rg -n "email_hash|phonenumber_hash|EncryptedStringTypeHandler|phonenumber like" manzhushaka-system/src/main/resources/mapper/system/SysUserMapper.xml
```

预期：能看到 `email_hash`、`phonenumber_hash`、`EncryptedStringTypeHandler`；不再出现 `phonenumber like`。

- [ ] **步骤 14：Commit**

```bash
git add manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/entity/SysUser.java \
  manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/support/SysUserSensitiveFieldSupport.java \
  manzhushaka-system/src/main/java/com/manzhushaka/system/mapper/SysUserMapper.java \
  manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/mapper/SysUserMapper.java \
  manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/SysUserServiceImpl.java \
  manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/repository/UserRepositoryImpl.java \
  manzhushaka-system/src/main/resources/mapper/system/SysUserMapper.xml \
  manzhushaka-system/src/test/java/com/manzhushaka/system/infrastructure/persistence/support/SysUserSensitiveFieldSupportTest.java
git commit -m "feat: 用户敏感字段接入存储加密"
```

---

### 任务 6：同步初始化 SQL

**文件：**
- 修改：`sql/manzhushaka_db_init.sql`

- [ ] **步骤 1：定位 sys_user 表结构**

运行：

```bash
rg -n "create table sys_user|email|phonenumber" sql/manzhushaka_db_init.sql
```

预期：能看到 `sys_user` 的 `email varchar(50)` 和 `phonenumber varchar(11)`。

- [ ] **步骤 2：扩大密文字段长度并增加 hash 列**

在 `sys_user` 建表语句中改为：

```sql
  email             varchar(512)    default ''                 comment '用户邮箱密文',
  email_hash        varchar(128)    default ''                 comment '用户邮箱检索摘要',
  phonenumber       varchar(512)    default ''                 comment '手机号码密文',
  phonenumber_hash  varchar(128)    default ''                 comment '手机号码检索摘要',
```

- [ ] **步骤 3：增加索引**

在 `sys_user` 建表语句的索引区域加入：

```sql
  key idx_email_hash (email_hash),
  key idx_phonenumber_hash (phonenumber_hash)
```

如果该表原本最后一个索引项没有逗号，按 MySQL 语法调整逗号位置。

- [ ] **步骤 4：检查初始化用户数据**

运行：

```bash
rg -n "insert into sys_user|sys_user values|email_hash|phonenumber_hash" sql/manzhushaka_db_init.sql
```

预期：如果初始化 SQL 使用全列 insert，需要同步补齐 `email_hash`、`phonenumber_hash` 的值；如果明确列名 insert，不需要补值。

- [ ] **步骤 5：Commit**

```bash
git add sql/manzhushaka_db_init.sql
git commit -m "chore: 同步用户敏感字段初始化 SQL"
```

---

### 任务 7：补充运行配置和安全文档

**文件：**
- 修改：`manzhushaka-admin/src/main/resources/application.yml`
- 创建：`docs/security/sensitive-encryption.md`

- [ ] **步骤 1：添加字段加密配置占位**

在 `application.yml` 中新增：

```yaml
manzhushaka:
  crypto:
    enabled: ${SENSITIVE_FIELD_CRYPTO_ENABLED:false}
    aes-key: ${SENSITIVE_FIELD_AES_KEY:}
    hmac-key: ${SENSITIVE_FIELD_HMAC_KEY:}
```

如果文件中已有 `manzhushaka:` 根节点，将 `crypto` 合并到现有根节点下，不创建重复根节点。

- [ ] **步骤 2：创建文档**

创建 `docs/security/sensitive-encryption.md`：

```markdown
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
```

- [ ] **步骤 3：验证文档无真实密钥**

运行：

```bash
rg -n "0123456789abcdef|1a2s3d4f|abcdefghijklmnopqrstuvwxyz|change-me" docs/security/sensitive-encryption.md manzhushaka-admin/src/main/resources/application.yml
```

预期：只允许 `change-me` 出现在文档示例中；不得出现真实仓库旧密码或旧 token secret。

- [ ] **步骤 4：Commit**

```bash
git add manzhushaka-admin/src/main/resources/application.yml docs/security/sensitive-encryption.md
git commit -m "docs: 补充敏感信息加密运行说明"
```

---

### 任务 8：全量验证和回归检查

**文件：**
- 不新增文件
- 检查：所有本计划涉及文件

- [ ] **步骤 1：运行 common 单元测试**

运行：

```bash
mvn -pl manzhushaka-common test
```

预期：common 模块测试全部通过。

- [ ] **步骤 2：运行 system 单元测试**

运行：

```bash
mvn -pl manzhushaka-system -am test
```

预期：system 及依赖模块测试全部通过。

- [ ] **步骤 3：运行 admin 编译验证**

运行：

```bash
mvn -pl manzhushaka-admin -am test
```

预期：admin 聚合测试通过。若公司 Maven 仓库连接重置，记录失败命令、错误仓库、artifact 和本地已完成的子模块测试结果。

- [ ] **步骤 4：敏感信息静态扫描**

运行：

```bash
rg -n "1a2s3d4f|abcdefghijklmnopqrstuvwxyz|SENSITIVE_FIELD_AES_KEY:([A-Za-z0-9+/=]{20,})|SENSITIVE_FIELD_HMAC_KEY:([A-Za-z0-9+/=]{20,})" .
```

预期：不出现旧明文密码、旧 token secret、硬编码字段加密密钥。

- [ ] **步骤 5：检查脱敏和日志**

运行：

```bash
rg -n "email|phonenumber|password|token|secret" manzhushaka-framework/src/main/java/com/manzhushaka/framework/interceptor manzhushaka-system/src/main/java manzhushaka-admin/src/main/java
```

预期：请求日志已有敏感参数脱敏；新增代码没有直接打印明文邮箱、手机号、密钥、密文全文。

- [ ] **步骤 6：检查 Git 差异**

运行：

```bash
git diff --stat
git diff --check
```

预期：只包含本计划范围内文件，`git diff --check` 没有空白错误。

- [ ] **步骤 7：最终 Commit**

如果任务 1-7 已经逐步 commit，且任务 8 只产生验证记录，不需要额外 commit。若修复了验证中发现的问题，按实际文件执行：

```bash
git add pom.xml manzhushaka-admin/pom.xml manzhushaka-common manzhushaka-system sql/manzhushaka_db_init.sql docs/security/sensitive-encryption.md
git commit -m "fix: 修正敏感信息加密回归问题"
```

---

## 实施风险和处理策略

- **历史数据迁移风险：** 旧数据仍是明文，TypeHandler 会把非 `v1:` 值当明文读出。上线后必须执行一次性迁移，把历史明文重写为密文并补 hash。
- **查询行为变化：** 手机号原模糊查询会变成精确查询，需要提前告知前端和业务侧。
- **密钥丢失风险：** AES 密钥丢失后无法解密历史数据，HMAC 密钥丢失后无法做精确匹配。生产密钥必须纳入密钥管理和备份流程。
- **脱敏不等于加密：** `@Sensitive` 只影响 JSON 输出，不能替代数据库加密。数据库加密后，VO 输出仍要继续脱敏。
- **密码字段例外：** 登录密码继续 BCrypt 哈希，不允许改成 AES 可逆加密。
