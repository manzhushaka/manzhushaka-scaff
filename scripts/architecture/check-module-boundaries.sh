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
    "admin controller 不引用 infrastructure.persistence.entity" \
    "infrastructure\\.persistence\\.entity" \
    "manzhushaka-admin/src/main/java"

# Rule 3: System application layer should not depend on web DTO/VO
check \
    "system application 不引用 web.dto / web.vo" \
    "web\\.dto|web\\.vo" \
    "manzhushaka-system/src/main/java"

echo ""
if [ $VIOLATIONS -eq 0 ]; then
    echo "🎉 All boundary checks passed!"
    exit 0
else
    echo "💥 $VIOLATIONS violation(s) found. See details above."
    exit 1
fi