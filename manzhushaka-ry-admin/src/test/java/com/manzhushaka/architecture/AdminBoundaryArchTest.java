package com.manzhushaka.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.freeze.FreezingArchRule.freeze;

/**
 * Admin 模块边界守护。
 *
 * @author manzhushaka
 * @date 2026-06-28
 */
@AnalyzeClasses(packages = "com.manzhushaka")
public class AdminBoundaryArchTest {

    @ArchTest
    static final ArchRule CONTROLLER_SHOULD_NOT_DEPEND_ON_ENTITY =
            freeze(noClasses()
                    .that().resideInAPackage("..web.controller..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure.persistence.entity.."))
                    .as("admin_controllers_should_not_depend_on_persistence_entity");

    @ArchTest
    static final ArchRule CONTROLLER_SHOULD_NOT_DEPEND_ON_MAPPER =
            noClasses()
                    .that().resideInAPackage("..web.controller..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..mapper..");
}