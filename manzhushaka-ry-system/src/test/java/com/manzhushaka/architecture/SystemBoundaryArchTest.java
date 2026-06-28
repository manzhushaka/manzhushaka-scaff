package com.manzhushaka.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * System 模块边界守护：
 * - system.application 禁止依赖 web.dto / web.vo（admin 层对象）
 * - system 中禁止依赖已删除的 common.core.domain.entity
 */
@AnalyzeClasses(packages = "com.manzhushaka")
public class SystemBoundaryArchTest {

    @ArchTest
    static final ArchRule APPLICATION_SHOULD_NOT_DEPEND_ON_WEB_DTO =
            noClasses()
                    .that().resideInAPackage("..system.application..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..web.dto..", "..web.vo..");

    @ArchTest
    static final ArchRule SYSTEM_SHOULD_NOT_DEPEND_ON_COMMON_ENTITY =
            noClasses()
                    .that().resideInAPackage("..system..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..common.core.domain.entity..");
}