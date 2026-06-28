package com.manzhushaka.web.vo.system.shared;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 树选择视图对象
 * <p>
 * 前端树下拉组件所需的视图结构，由 {@code TreeSelectAdminConverter}
 * 从 {@code TreeNodeResult} 转换而来。
 * </p>
 *
 * @author manzhushaka
 */
public class TreeSelectVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 节点 ID */
    private Long id;

    /** 节点名称 */
    private String label;

    /** 节点禁用 */
    private boolean disabled = false;

    /** 子节点 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelectVo> children;

    public TreeSelectVo()
    {
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public boolean isDisabled()
    {
        return disabled;
    }

    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }

    public List<TreeSelectVo> getChildren()
    {
        return children;
    }

    public void setChildren(List<TreeSelectVo> children)
    {
        this.children = children;
    }
}