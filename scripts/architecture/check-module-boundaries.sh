#!/usr/bin/env bash
# ------------------------------------------------------------------
# Module boundary quick check script
# Usage: bash scripts/architecture/check-module-boundaries.sh
#
# This script performs regex-based boundary checks to ensure
# no module violates the defined dependency rules.
#
# Exit code 0 = all checks pass
# Exit code 1 = violation(s) found
# ------------------------------------------------------------------
set -euo pipefail

VIOLATIONS=0

check() {
    local description="$1"
    local search_pattern="$2"
    local search_path="$3"

    if rg -n "$search_pattern" $search_path 2>/dev/null; then
        echo "❌ VIOLATION: $description"
        echo "   Found matches in: $search_path"
        echo "   Pattern: $search_pattern"
        VIOLATIONS=$((VIOLATIONS + 1))
    else
        echo "✅ PASS: $description"
    fi
}

echo "=== Module Boundary Quick Check ==="
echo ""

# Rule 1: No module should reference common.core.domain.entity (fully migrated)
check \
    "common.core.domain.entity 引用已清除" \
    "common\\.core\\.domain\\.entity\\." \
    "manzhushaka-admin/src/main/java manzhushaka-framework/src/main/java manzhushaka-system/src/main/java manzhushaka-common/src/main/java"

# Rule 2: Admin controllers should not depend on persistence entities directly
check \
    "pii admin controller 不引用 infrastructure.persistence.entity" \
    "infrastructure\\.persistence\\.entity" \
    "manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/pii"

# Rule 3: System application layer should not depend on web DTO/VO
check \
    "system application 不引用 web.dto / web.vo" \
    "web\\.dto|web\\.vo" \
    "manzhushaka-system/src/main/java"

# Rule 4: Common module should not contain PII business code
check \
    "common 模块不得引用 pii 业务代码" \
    "\\bpii\\b|Pii" \
    "manzhushaka-common/src/main/java"

# Rule 5: PII business module should not depend on admin web layer
check \
    "pii 模块禁止引用 admin web 层" \
    "com\\.manzhushaka\\.web" \
    "manzhushaka-biz-pii/src/main/java"

if rg -n "com\\.manzhushaka\\.biz\\.pii" manzhushaka-admin/src/main/java 2>/dev/null; then
    echo "OK: admin 引用 pii（正常）"
fi

echo ""
if [ $VIOLATIONS -eq 0 ]; then
    echo "OK: pii 模块边界检查通过"
    echo "🎉 All boundary checks passed!"
    exit 0
else
    echo "💥 $VIOLATIONS violation(s) found. See details above."
    exit 1
fi
