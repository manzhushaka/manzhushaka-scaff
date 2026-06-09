package com.manzhushaka.common.enums;

public enum DataScopeType {
    SELF(1),
    DEPT(2),
    DEPT_AND_CHILD(3),
    ALL(4);

    private final int level;

    DataScopeType(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

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
