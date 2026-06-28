package com.manzhushaka.system.application.result.shared;

import java.util.List;

/**
 * 树节点结果
 * <p>
 * 表示一棵树的节点，包含 ID、标签、禁用状态和子节点列表。
 * 作为 system 模块的内部 DTO，由 admin 模块的 {@code TreeSelectAdminConverter}
 * 转换为前端所需的 {@code TreeSelectVo}。
 * </p>
 *
 * @param id       节点 ID
 * @param label    节点名称
 * @param disabled 是否禁用
 * @param children 子节点列表
 */
public record TreeNodeResult(Long id, String label, boolean disabled, List<TreeNodeResult> children)
{
}