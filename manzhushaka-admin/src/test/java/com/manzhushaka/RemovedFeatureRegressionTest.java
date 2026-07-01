package com.manzhushaka;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 已删除功能回归测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class RemovedFeatureRegressionTest
{
    private static final String INIT_SQL_PATH = "sql/manzhushaka_db_init.sql";

    private static final List<String> REMOVED_POST_CLASS_NAMES = List.of(
            "com.manzhushaka.web.controller.system.SysPostController",
            "com.manzhushaka.system.service.ISysPostService",
            "com.manzhushaka.system.service.impl.SysPostServiceImpl",
            "com.manzhushaka.system.mapper.SysPostMapper",
            "com.manzhushaka.system.mapper.SysUserPostMapper",
            "com.manzhushaka.system.domain.SysPost",
            "com.manzhushaka.system.domain.SysUserPost",
            "com.manzhushaka.system.infrastructure.persistence.mapper.SysPostMapper",
            "com.manzhushaka.system.infrastructure.persistence.mapper.SysUserPostMapper",
            "com.manzhushaka.system.infrastructure.persistence.entity.SysPost",
            "com.manzhushaka.system.infrastructure.persistence.entity.SysUserPost");

    private static final List<String> REMOVED_NOTICE_CLASS_NAMES = List.of(
            "com.manzhushaka.web.controller.system.SysNoticeController",
            "com.manzhushaka.system.service.ISysNoticeService",
            "com.manzhushaka.system.service.ISysNoticeReadService",
            "com.manzhushaka.system.service.impl.SysNoticeServiceImpl",
            "com.manzhushaka.system.service.impl.SysNoticeReadServiceImpl",
            "com.manzhushaka.system.mapper.SysNoticeMapper",
            "com.manzhushaka.system.mapper.SysNoticeReadMapper",
            "com.manzhushaka.system.domain.SysNotice",
            "com.manzhushaka.system.domain.SysNoticeRead",
            "com.manzhushaka.system.infrastructure.persistence.mapper.SysNoticeMapper",
            "com.manzhushaka.system.infrastructure.persistence.mapper.SysNoticeReadMapper",
            "com.manzhushaka.system.infrastructure.persistence.entity.SysNotice",
            "com.manzhushaka.system.infrastructure.persistence.entity.SysNoticeRead");

    private static final List<String> REMOVED_NOTICE_FRONTEND_FILES = List.of(
            "ui-admin/src/api/system/notice.js",
            "ui-admin/src/layout/components/HeaderNotice/index.vue",
            "ui-admin/src/layout/components/HeaderNotice/DetailView.vue",
            "ui-admin/src/views/system/notice/index.vue",
            "ui-admin/src/views/system/notice/ReadUsers.vue");

    private static final List<String> REMOVED_POST_FRONTEND_FILES = List.of(
            "ui-admin/src/api/system/post.js",
            "ui-admin/src/views/system/post/index.vue");

    private static final List<String> REMOVED_REQUEST_LOG_FRONTEND_FILES = List.of(
            "ui-admin/src/api/monitor/requestLog.js",
            "ui-admin/src/views/monitor/requestLog/index.vue");

    /**
     * 岗位管理相关后端类应已从类路径移除。
     */
    @Test
    void postFeatureBackendClassesShouldBeRemoved()
    {
        for (String className : REMOVED_POST_CLASS_NAMES)
        {
            assertThat(isClassPresent(className))
                    .as("class should be absent: %s", className)
                    .isFalse();
        }
    }

    /**
     * 岗位管理相关前端文件应已从仓库移除。
     *
     * @throws IOException 读取路径失败
     */
    @Test
    void postFeatureFrontendFilesShouldBeRemoved() throws IOException
    {
        Path root = repositoryRoot();
        for (String relativePath : REMOVED_POST_FRONTEND_FILES)
        {
            assertThat(Files.exists(root.resolve(relativePath)))
                    .as("file should be absent: %s", relativePath)
                    .isFalse();
        }
    }

    /**
     * 通知公告相关后端类应已从类路径移除。
     */
    @Test
    void noticeFeatureBackendClassesShouldBeRemoved()
    {
        for (String className : REMOVED_NOTICE_CLASS_NAMES)
        {
            assertThat(isClassPresent(className))
                    .as("class should be absent: %s", className)
                    .isFalse();
        }
    }

    /**
     * 通知公告相关前端文件应已从仓库移除。
     *
     * @throws IOException 读取路径失败
     */
    @Test
    void noticeFeatureFrontendFilesShouldBeRemoved() throws IOException
    {
        Path root = repositoryRoot();
        for (String relativePath : REMOVED_NOTICE_FRONTEND_FILES)
        {
            assertThat(Files.exists(root.resolve(relativePath)))
                    .as("file should be absent: %s", relativePath)
                    .isFalse();
        }
    }

    /**
     * 请求日志相关前端文件应已从仓库移除。
     *
     * @throws IOException 读取路径失败
     */
    @Test
    void requestLogFrontendFilesShouldBeRemoved() throws IOException
    {
        Path root = repositoryRoot();
        for (String relativePath : REMOVED_REQUEST_LOG_FRONTEND_FILES)
        {
            assertThat(Files.exists(root.resolve(relativePath)))
                    .as("file should be absent: %s", relativePath)
                    .isFalse();
        }
    }

    /**
     * 初始化 SQL 不应再保留通知公告与若依官网初始化数据。
     *
     * @throws IOException 读取 SQL 文件失败
     */
    @Test
    void initializationSqlShouldNotContainRemovedNoticeOrRuoyiMenu() throws IOException
    {
        String sql = Files.readString(repositoryRoot().resolve(INIT_SQL_PATH));

        assertThat(sql)
                .doesNotContain("若依官网")
                .doesNotContain("http://ruoyi.vip")
                .doesNotContain("通知公告")
                .doesNotContain("system:notice:")
                .doesNotContain("sys_notice_type")
                .doesNotContain("sys_notice_status")
                .doesNotContain("sys_notice_read")
                .doesNotContain("sys_notice ")
                .doesNotContain("岗位管理")
                .doesNotContain("system:post:")
                .doesNotContain("sys_post")
                .doesNotContain("sys_user_post");
    }

    /**
     * 初始化 SQL 应保留系统监控日志中心菜单及对应权限闭环。
     *
     * @throws IOException 读取 SQL 文件失败
     */
    @Test
    void initializationSqlShouldContainMonitorLogCenterMenusAndPermissions() throws IOException
    {
        String sql = Files.readString(repositoryRoot().resolve(INIT_SQL_PATH));

        assertThat(sql)
                .contains("insert into sys_menu values('108',  '日志中心', '2'")
                .contains("monitor/logCenter/index")
                .contains("monitor/runtimeLog/index")
                .contains("monitor/slowSql/index")
                .contains("monitor:logcenter:list")
                .contains("monitor:runtimelog:list")
                .contains("monitor:slowsql:list")
                .contains("monitor:operlog:list")
                .contains("monitor:logininfor:list")
                .doesNotContain("monitor/operlog/index")
                .doesNotContain("monitor/logininfor/index")
                .doesNotContain("monitor/requestLog/index")
                .doesNotContain("monitor:requestlog:list")
                .doesNotContain("sys_request_log")
                .contains("monitor:logininfor:unlock");
    }

    /**
     * 解析仓库根目录。
     *
     * @return 仓库根目录
     */
    private Path repositoryRoot()
    {
        Path currentDirectory = Path.of("").toAbsolutePath().normalize();
        Path sqlPath = currentDirectory.resolve(INIT_SQL_PATH);
        if (Files.exists(sqlPath))
        {
            return currentDirectory;
        }
        return currentDirectory.getParent();
    }

    /**
     * 判断指定类是否仍然存在。
     *
     * @param className 完整类名
     * @return true 表示类仍然存在
     */
    private boolean isClassPresent(String className)
    {
        try
        {
            Class.forName(className);
            return true;
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }
}
