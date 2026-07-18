package com.manzhushaka.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Admin 模块边界守护。
 *
 * @author manzhushaka
 * @date 2026-06-28
 */
@AnalyzeClasses(packages = "com.manzhushaka", importOptions = ImportOption.DoNotIncludeTests.class)
public class AdminBoundaryArchTest
{

    @ArchTest
    static final ArchRule CONTROLLER_SHOULD_NOT_DEPEND_ON_ENTITY =
            noClasses()
                    .that().resideInAPackage("..web.controller..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure.persistence.entity..");

    @ArchTest
    static final ArchRule CONTROLLER_SHOULD_NOT_DEPEND_ON_LEGACY_DOMAIN =
            noClasses()
                    .that().resideInAPackage("..web.controller..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..system.domain..");

    @ArchTest
    static final ArchRule CONTROLLER_SHOULD_NOT_DEPEND_ON_MAPPER =
            noClasses()
                    .that().resideInAPackage("..web.controller..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..mapper..");

    @ArchTest
    static final ArchRule USER_CONTROLLER_SHOULD_USE_APPLICATION_SERVICE =
            noClasses()
                    .that().haveSimpleName("SysUserController")
                    .should().dependOnClassesThat()
                    .haveFullyQualifiedName("com.manzhushaka.system.service.ISysUserService");
}
