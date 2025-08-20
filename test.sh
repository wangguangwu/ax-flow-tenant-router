#!/usr/bin/env bash
# 多租户支付路由回归脚本（纯净版）
# 仅以 ApiResult.code 断言；依赖：curl、jq

BASE_URL="http://localhost:8080"
TENANT_HEADER="X-Tenant-Id"
CURL="curl -sS --max-time 10"

PASS=0
FAIL=0
FAILED=()

hr(){ printf '%*s\n' 80 | tr ' ' '-'; }
ok(){ printf "\033[32m%s\033[0m\n" "$*"; }
bad(){ printf "\033[31m%s\033[0m\n" "$*"; }

ali()    { printf '{"amount":"%s","sellerId":"%s","appId":"%s"}' "$1" "$2" "$3"; }
wechat() { printf '{"amount":"%s","mchId":"%s","appId":"%s"}'   "$1" "$2" "$3"; }

# name method path tenant body expect_apiCode
run_case() {
  local name="$1" method="$2" path="$3" tenant="$4" body="$5" expect="$6"

  echo
  hr
  echo "[CASE] $name"
  echo "[REQ ] $method $BASE_URL$path"
  [[ -n "$tenant" ]] && echo "[HEAD] $TENANT_HEADER: $tenant" || echo "[HEAD] <none>"
  [[ -n "$body"   ]] && echo "[BODY] $body" || echo "[BODY] <empty>"

  local resp
  if [[ -n "$tenant" ]]; then
    resp="$($CURL -X "$method" "$BASE_URL$path" \
      -H "Content-Type: application/json" \
      -H "$TENANT_HEADER: $tenant" \
      -d "$body")"
  else
    resp="$($CURL -X "$method" "$BASE_URL$path" \
      -H "Content-Type: application/json" \
      -d "$body")"
  fi

  echo "[RESP] $resp"

  local code
  code="$(echo "$resp" | jq -r '.code // empty' 2>/dev/null)"

  if [[ "$code" == "$expect" ]]; then
    ok "[RESULT] PASS"
    ((PASS++))
  else
    bad "[RESULT] FAIL  expect=$expect  got=${code:-<none>}"
    FAILED+=("$name")
    ((FAIL++))
  fi
}

echo "== Ax-Flow Tenant Router（ApiResult.code 断言）=="
echo "Base URL   : $BASE_URL"
echo "Header Name: $TENANT_HEADER"
echo

# ---------------------- 全量用例 ----------------------

# 正常路由（期望成功 200）
run_case "submit - TenantA -> AliPay OK"   POST /payment/submit       TenantA "$(ali   100.00  A-SELLER-001  A-APP-001)" 200
run_case "submit - TenantB -> WeChat OK"   POST /payment/submit       TenantB "$(wechat 200.00  W-MCH-001     W-APP-001)" 200

# 白名单 onlyA（TenantA 允许，TenantB 禁止）
run_case "onlyA - TenantA OK"              POST /payment/submit/onlyA TenantA "$(ali    88.00  A-SELLER-002  A-APP-002)" 200
run_case "onlyA - TenantB forbidden"       POST /payment/submit/onlyA TenantB "$(wechat 88.00  W-MCH-002     W-APP-002)" 400

# 黑名单 denyA（TenantA 禁止，TenantB 允许）
run_case "denyA - TenantA forbidden"       POST /payment/submit/denyA TenantA "$(ali    50.00  A-SELLER-003  A-APP-003)" 400
run_case "denyA - TenantB OK"              POST /payment/submit/denyA TenantB "$(wechat 50.00  W-MCH-003     W-APP-003)" 200

# 业务校验失败（AliPay：前缀/相等/必填）
run_case "AliPay 校验失败：sellerId 未以 A 开头"   POST /payment/submit TenantA "$(ali   10.00  X-SELLER     A-APP-009)" 400
run_case "AliPay 校验失败：sellerId 与 appId 相同" POST /payment/submit TenantA "$(ali   10.00  A-SAME       A-SAME)"    400
run_case "AliPay 校验失败：sellerId 为空"         POST /payment/submit TenantA "$(ali   10.00  ""           A-APP-EMPTY)" 400
run_case "AliPay 校验失败：appId 为空"            POST /payment/submit TenantA "$(ali   10.00  A-SELLER-EMPTY  "")"      400

# 业务校验失败（WeChat：前缀/相等/必填）
run_case "WeChat 校验失败：mchId 未以 W 开头"     POST /payment/submit TenantB "$(wechat 10.00  X-MCH       W-APP-009)" 400
run_case "WeChat 校验失败：mchId 与 appId 相同"   POST /payment/submit TenantB "$(wechat 10.00  W-SAME      W-SAME)"    400
run_case "WeChat 校验失败：mchId 为空"            POST /payment/submit TenantB "$(wechat 10.00  ""          W-APP-EMPTY)" 400
run_case "WeChat 校验失败：appId 为空"            POST /payment/submit TenantB "$(wechat 10.00  W-MCH-EMPTY  "")"        400

# 缺少租户头（默认拒绝）
run_case "缺少租户头（默认拒绝）"                 POST /payment/submit ""     "$(ali   10.00  A-SELLER-010 A-APP-010)" 400

# 空 body（要求 bodyRequired=true）
run_case "空 body（bodyRequired=true）"            POST /payment/submit TenantA "" 400

# ---------------------- 汇总 ----------------------
hr
echo "SUMMARY: PASS=$PASS, FAIL=$FAIL"
if ((FAIL > 0)); then
  echo "FAILED CASES:"
  for c in "${FAILED[@]}"; do
    echo " - $c"
  done
fi
exit $FAIL