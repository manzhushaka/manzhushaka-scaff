package com.manzhushaka.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Admin 模块边界守护：
 * - Controller 禁止直接依赖 infrastructure.persistence.entity
 * - Controller 禁止直接依赖 mapper
 */
@AnalyzeClasses(packages = "com.manzhushaka")
public class AdminBoundaryArchTest {

    @ArchTest
    static final ArchRule CONTROLLER_SHOULD_NOT_DEPEND_ON_ENTITY =
            noClasses()
                    .that().resideInAPackage("..web.controller..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure.persistence.entity..", "..mapper..");

    @ArchTest
    static final ArchRule CONTROLLER_SHOULD_NOT_DEPEND_ON_MAPPER =
            noClasses()
                    .that().resideInAPackage("..web.controller..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..mapper..");
}