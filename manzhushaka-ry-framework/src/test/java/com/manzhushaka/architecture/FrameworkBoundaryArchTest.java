package com.manzhushaka.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Framework 模块边界守护：
 * - framework 中禁止引用已删除的 common.core.domain.entity
 * - framework 中禁止引用 common.core.domain.model（LoginUser/LoginBody等已迁移或删除的模型）
 */
@AnalyzeClasses(packages = "com.manzhushaka")
public class FrameworkBoundaryArchTest {

    @ArchTest
    static final ArchRule FRAMEWORK_SHOULD_NOT_DEPEND_ON_COMMON_ENTITY =
            noClasses()
                    .that().resideInAPackage("..framework..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..common.core.domain.entity..", "..common.core.domain.model..");

    @ArchTest
    static final ArchRule FRAMEWORK_SHOULD_NOT_DEPEND_ON_WEB_DTO =
            noClasses()
                    .that().resideInAPackage("..framework..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..web.dto..", "..web.vo..");
}