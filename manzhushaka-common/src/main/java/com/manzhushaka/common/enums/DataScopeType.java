package com.manzhushaka.common.enums;

/**
 * 定义 DataScopeType 枚举值。
 */
public enum DataScopeType {
    SELF(1),
    DEPT(2),
    DEPT_AND_CHILD(3),
    ALL(4);

    private final int level;

    DataScopeType(int level) {
        this.level = level;
    }

    /**
     * 返回 level。
     *
     * @return 字段值
     */
    public int getLevel() {
        return level;
    }

    /**
     * 执行 max 逻辑。
     *
     * @param left left 参数
     * @param right right 参数
     * @return 处理结果
     */
    public static DataScopeType max(DataScopeType left, DataScopeType right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.level >= right.level ? left : right;
    }
}
