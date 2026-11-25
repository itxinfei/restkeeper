#!/usr/bin/env bash
# 将 deploy/nacos/*.yml 导入 Nacos（先启动中间件）
# 地址/凭据以《部署手册-中间件与数据》§3 为准：默认 http://192.168.20.88:8848。
# Nacos 3.x：v1 console 配置接口已下线（404），改用 v3 Admin API；失败回退 v1。
# 鉴权开启时先用 NACOS_USERNAME/NACOS_PASSWORD 登录取 accessToken（或直接给 NACOS_ACCESS_TOKEN）。
set -euo pipefail
NACOS="${NACOS_SERVER:-192.168.20.88:8848}"
GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
DIR="$(cd "$(dirname "$0")" && pwd)"

# 获取 accessToken（未显式提供时登录）
if [[ -z "${NACOS_ACCESS_TOKEN:-}" ]]; then
  NACOS_ACCESS_TOKEN="$(curl -s -X POST "http://$NACOS/nacos/v1/auth/login" \
    --data-urlencode "username=${NACOS_USERNAME:-nacos}" \
    --data-urlencode "password=${NACOS_PASSWORD:-123456}" \
    | sed -n 's/.*"accessToken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
  if [[ -n "$NACOS_ACCESS_TOKEN" ]]; then echo "Nacos 登录成功（${NACOS_USERNAME:-nacos}）"; fi
fi
TOKEN_ARGS=(); [[ -n "${NACOS_ACCESS_TOKEN:-}" ]] && TOKEN_ARGS=(--data-urlencode "accessToken=$NACOS_ACCESS_TOKEN")

publish() { # $1=apiBase $2=dataId $3=groupParamName $4=group $5=file
  local api="$1" did="$2" gp="$3" grp="$4" f="$5"
  local contentPath; contentPath="$(cygpath -w "$f" 2>/dev/null || echo "$f")"
  curl -s -X POST "$api" \
    --data-urlencode "dataId=$did" \
    --data-urlencode "$gp=$grp" \
    --data-urlencode "type=yaml" \
    "${TOKEN_ARGS[@]}" \
    --data-urlencode "content@$contentPath"
}

for f in "$DIR"/*.yml; do
  dataId="$(basename "$f")"
  resp="$(publish "http://$NACOS/nacos/v3/admin/cs/config" "$dataId" groupName "$GROUP" "$f")"
  if printf '%s' "$resp" | grep -q '"code":0'; then
    echo "导入 $dataId -> $NACOS ($GROUP) [v3]"
  else
    resp="$(publish "http://$NACOS/nacos/v1/cs/configs" "$dataId" group "$GROUP" "$f")"
    echo "导入 $dataId -> $NACOS ($GROUP) [v1 回退] $resp"
  fi
done
echo "完成。可在控制台查看（Nacos 3.x 控制台端口 8080：http://$NACOS/nacos → 或 :8080/nacos）。"
