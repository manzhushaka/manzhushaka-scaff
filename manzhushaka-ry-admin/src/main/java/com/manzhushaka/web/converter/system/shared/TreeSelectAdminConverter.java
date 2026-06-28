package com.manzhushaka.web.converter.system.shared;

import java.util.Collections;
import java.util.List;
import com.manzhushaka.system.application.result.shared.TreeNodeResult;
import com.manzhushaka.web.vo.system.shared.TreeSelectVo;

/**
 * 树选择转换器
 * <p>
 * 将 system 模块的 {@link TreeNodeResult} 转换为 admin 模块的 {@link TreeSelectVo}。
 * </p>
 *
 * @author manzhushaka
 */
public final class TreeSelectAdminConverter
{
    private TreeSelectAdminConverter()
    {
    }

    /**
     * 将单个树节点转换为 TreeSelectVo
     *
     * @param node 树节点
     * @return TreeSelectVo
     */
    public static TreeSelectVo toVo(TreeNodeResult node)
    {
        if (node == null)
        {
            return null;
        }
        TreeSelectVo vo = new TreeSelectVo();
        vo.setId(node.id());
        vo.setLabel(node.label());
        vo.setDisabled(node.disabled());
        if (node.children() != null && !node.children().isEmpty())
        {
            vo.setChildren(toVoList(node.children()));
        }
        return vo;
    }

    /**
     * 将树节点列表转换为 TreeSelectVo 列表
     *
     * @param nodes 树节点列表
     * @return TreeSelectVo 列表
     */
    public static List<TreeSelectVo> toVoList(List<TreeNodeResult> nodes)
    {
        if (nodes == null || nodes.isEmpty())
        {
            return Collections.emptyList();
        }
        return nodes.stream().map(TreeSelectAdminConverter::toVo).toList();
    }
}