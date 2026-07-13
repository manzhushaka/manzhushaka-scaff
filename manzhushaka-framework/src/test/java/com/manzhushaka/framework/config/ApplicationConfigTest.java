package com.manzhushaka.framework.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.type.Alias;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 程序注解配置测试。
 *
 * @author manzhushaka
 * @date 2026-06-28
 */
class ApplicationConfigTest
{
    private static final String TYPE_ALIASES_PACKAGE =
            "com.manzhushaka.**.domain,com.manzhushaka.**.infrastructure.persistence.entity";
    /**
     * Mapper 扫描包中不应存在简单类名相同的接口，避免启动时生成重复 bean 名。
     */
    @Test
    void mapperScanPackagesShouldNotContainDuplicateSimpleNames()
    {
        Map<String, List<String>> mapperClassNames = scanMapperClassNames();
        Map<String, List<String>> duplicateClassNames = new LinkedHashMap<>();
        mapperClassNames.forEach((simpleName, classNames) -> {
            if (classNames.size() > 1)
            {
                duplicateClassNames.put(simpleName, classNames);
            }
        });

        assertThat(duplicateClassNames).isEmpty();
    }

    /**
     * MyBatis 类型别名扫描包中不应存在重复别名，避免启动时别名注册冲突。
     */
    @Test
    void typeAliasPackagesShouldNotContainDuplicateAliases()
    {
        Map<String, List<String>> aliasClassNames = scanTypeAliasClassNames();
        Map<String, List<String>> duplicateAliasClassNames = new LinkedHashMap<>();
        aliasClassNames.forEach((alias, classNames) -> {
            if (classNames.size() > 1)
            {
                duplicateAliasClassNames.put(alias, classNames);
            }
        });

        assertThat(duplicateAliasClassNames).isEmpty();
    }

    /**
     * 扫描 {@link ApplicationConfig} 声明的 Mapper 包，按简单类名聚合接口类名。
     *
     * @return Mapper 简单类名到完整类名列表的映射
     */
    private Map<String, List<String>> scanMapperClassNames()
    {
        Map<String, List<String>> mapperClassNames = new LinkedHashMap<>();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);
        for (String packageName : mapperScanPackages())
        {
            String mapperPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + ClassUtils.convertClassNameToResourcePath(packageName) + "/**/*.class";
            for (Resource resource : resources(resolver, mapperPattern))
            {
                if (resource.isReadable())
                {
                    MetadataReader metadataReader = metadataReader(metadataReaderFactory, resource);
                    if (metadataReader.getClassMetadata().isInterface())
                    {
                        String className = metadataReader.getClassMetadata().getClassName();
                        mapperClassNames.computeIfAbsent(simpleName(className), key -> new ArrayList<>()).add(className);
                    }
                }
            }
        }
        return mapperClassNames;
    }

    /**
     * 扫描 MyBatis 类型别名包，按最终别名聚合类名。
     *
     * @return MyBatis 类型别名到完整类名列表的映射
     */
    private Map<String, List<String>> scanTypeAliasClassNames()
    {
        Map<String, List<String>> aliasClassNames = new LinkedHashMap<>();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);
        String typeAliasesPackage = MyBatisConfig.setTypeAliasesPackage(TYPE_ALIASES_PACKAGE);
        for (String packageName : typeAliasesPackage.split(","))
        {
            String typeAliasPattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + ClassUtils.convertClassNameToResourcePath(packageName) + "/**/*.class";
            for (Resource resource : resources(resolver, typeAliasPattern))
            {
                if (resource.isReadable())
                {
                    MetadataReader metadataReader = metadataReader(metadataReaderFactory, resource);
                    String className = metadataReader.getClassMetadata().getClassName();
                    if (!className.endsWith("package-info"))
                    {
                        List<String> classNames = aliasClassNames.computeIfAbsent(typeAlias(className),
                                key -> new ArrayList<>());
                        if (!classNames.contains(className))
                        {
                            classNames.add(className);
                        }
                    }
                }
            }
        }
        return aliasClassNames;
    }

    /**
     * 按资源模式查找 class 文件。
     *
     * @param resolver 资源解析器
     * @param mapperPattern Mapper class 资源模式
     * @return 匹配的资源
     */
    private Resource[] resources(ResourcePatternResolver resolver, String mapperPattern)
    {
        try
        {
            return resolver.getResources(mapperPattern);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("扫描 Mapper 资源失败：" + mapperPattern, e);
        }
    }

    /**
     * 读取 class 元数据。
     *
     * @param metadataReaderFactory 元数据读取器工厂
     * @param resource class 资源
     * @return class 元数据读取器
     */
    private MetadataReader metadataReader(MetadataReaderFactory metadataReaderFactory, Resource resource)
    {
        try
        {
            return metadataReaderFactory.getMetadataReader(resource);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("读取 Mapper 元数据失败：" + resource, e);
        }
    }

    /**
     * 读取 {@link ApplicationConfig} 上声明的 Mapper 扫描包。
     *
     * @return Mapper 扫描包列表
     */
    private List<String> mapperScanPackages()
    {
        MapperScan mapperScan = ApplicationConfig.class.getAnnotation(MapperScan.class);
        List<String> packageNames = new ArrayList<>();
        packageNames.addAll(Arrays.asList(mapperScan.value()));
        packageNames.addAll(Arrays.asList(mapperScan.basePackages()));
        return packageNames;
    }

    /**
     * 获取类名的简单名称。
     *
     * @param className 完整类名
     * @return 简单类名
     */
    private String simpleName(String className)
    {
        int lastDotIndex = className.lastIndexOf('.');
        return className.substring(lastDotIndex + 1);
    }

    /**
     * 计算 MyBatis 注册类时使用的别名。
     *
     * @param className 完整类名
     * @return 小写后的类型别名
     */
    private String typeAlias(String className)
    {
        try
        {
            Class<?> aliasClass = Class.forName(className);
            Alias alias = aliasClass.getAnnotation(Alias.class);
            if (alias != null)
            {
                return alias.value().toLowerCase(Locale.ENGLISH);
            }
            return simpleName(className).toLowerCase(Locale.ENGLISH);
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException("读取 MyBatis 类型别名失败：" + className, e);
        }
    }
}
