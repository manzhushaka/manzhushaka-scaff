package com.manzhushaka;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 日志路径初始化器。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
final class LogPathInitializer {

    private static final String LOG_PATH_PROPERTY = "LOG_PATH";

    private static final String SPRING_PROFILES_ACTIVE_PROPERTY = "spring.profiles.active";

    private static final String SPRING_PROFILES_ACTIVE_ENV = "SPRING_PROFILES_ACTIVE";

    private static final String DEV_PROFILE = "dev";

    private static final String JAR_SUFFIX = ".jar";

    private static final String LOG_DIR_NAME = "logs";

    private LogPathInitializer() {
    }

    /**
     * 在 Spring Boot 初始化日志系统前设置日志目录。
     *
     * @param applicationClass 启动类
     * @param args 启动参数
     */
    static void initialize(Class<?> applicationClass, String[] args) {
        String configuredLogPath = firstNotBlank(System.getProperty(LOG_PATH_PROPERTY), System.getenv(LOG_PATH_PROPERTY));
        if (isNotBlank(configuredLogPath)) {
            System.setProperty(LOG_PATH_PROPERTY, Paths.get(configuredLogPath).toAbsolutePath().normalize().toString());
            return;
        }

        Path applicationPath = resolveApplicationPath(applicationClass);
        Path userDir = Paths.get(System.getProperty("user.dir"));
        String activeProfiles = resolveActiveProfiles(args);
        boolean packagedJar = Files.isRegularFile(applicationPath);
        Path logPath = resolveLogPath(activeProfiles, applicationPath, packagedJar, userDir);
        System.setProperty(LOG_PATH_PROPERTY, logPath.toString());
    }

    /**
     * 根据运行环境解析日志目录。
     *
     * @param activeProfiles 当前激活环境
     * @param applicationPath 应用代码来源路径
     * @param packagedJar 是否以 jar 文件运行
     * @param userDir 当前工作目录
     * @return 日志目录
     */
    static Path resolveLogPath(String activeProfiles, Path applicationPath, boolean packagedJar, Path userDir) {
        if (isDevProfile(activeProfiles)) {
            return userDir.resolve(LOG_DIR_NAME).toAbsolutePath().normalize();
        }
        if (packagedJar && applicationPath.getParent() != null) {
            return applicationPath.getParent().resolve(LOG_DIR_NAME).toAbsolutePath().normalize();
        }
        return userDir.resolve(LOG_DIR_NAME).toAbsolutePath().normalize();
    }

    /**
     * 解析当前激活环境。
     *
     * @param args 启动参数
     * @return 激活环境
     */
    private static String resolveActiveProfiles(String[] args) {
        String profiles = resolveProfilesFromArgs(args);
        if (isNotBlank(profiles)) {
            return profiles;
        }
        return firstNotBlank(System.getProperty(SPRING_PROFILES_ACTIVE_PROPERTY),
                System.getenv(SPRING_PROFILES_ACTIVE_ENV), DEV_PROFILE);
    }

    /**
     * 从命令行参数解析激活环境。
     *
     * @param args 启动参数
     * @return 激活环境
     */
    private static String resolveProfilesFromArgs(String[] args) {
        if (args == null) {
            return null;
        }
        String prefix = "--" + SPRING_PROFILES_ACTIVE_PROPERTY + "=";
        for (String arg : args) {
            if (arg != null && arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        return null;
    }

    /**
     * 解析应用代码来源路径。
     *
     * @param applicationClass 启动类
     * @return 代码来源路径
     */
    private static Path resolveApplicationPath(Class<?> applicationClass) {
        try {
            return Paths.get(applicationClass.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (IllegalArgumentException | URISyntaxException ex) {
            return resolveJarPathFromCommand();
        }
    }

    /**
     * 从 Java 启动命令中解析 jar 路径。
     *
     * @return jar 路径，无法解析时返回当前工作目录
     */
    private static Path resolveJarPathFromCommand() {
        String command = System.getProperty("sun.java.command");
        if (!isNotBlank(command)) {
            return Paths.get(System.getProperty("user.dir"));
        }
        String firstArg = command.trim().split("\\s+")[0];
        if (firstArg.endsWith(JAR_SUFFIX)) {
            return Paths.get(firstArg).toAbsolutePath().normalize();
        }
        return Paths.get(System.getProperty("user.dir"));
    }

    /**
     * 判断是否为本机开发环境。
     *
     * @param activeProfiles 当前激活环境
     * @return true 表示本机开发环境
     */
    private static boolean isDevProfile(String activeProfiles) {
        if (!isNotBlank(activeProfiles)) {
            return true;
        }
        String[] profiles = activeProfiles.split(",");
        for (String profile : profiles) {
            if (DEV_PROFILE.equalsIgnoreCase(profile.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回第一个非空字符串。
     *
     * @param values 候选字符串
     * @return 第一个非空字符串
     */
    private static String firstNotBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 判断字符串是否非空。
     *
     * @param value 字符串
     * @return true 表示非空
     */
    private static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
