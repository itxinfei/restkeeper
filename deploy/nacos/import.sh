#!/usr/bin/env bash
# 将 deploy/nacos/*.yml 导入 Nacos（先启动中间件）
# 地址/凭据以《部署手册-中间件与数据》§3 为准：默认 http://192.168.20.88:8848。
# Nacos 3.x 已下线 v1 console 配置接口（返回 404），改用 v3 Admin API；失败时回退 v1。
set -euo pipefail
NACOS="${NACOS_SERVER:-192.168.20.88:8848}"
GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
DIR="$(cd "$(dirname "$0")" && pwd)"

publish() { # $1=apiBase $2=dataId $3=groupParamName $4=group
  local api="$1" did="$2" gp="$3" grp="$4" f="$5"
  # Git Bash 下 curl 不认 /d/... 的 MSYS 路径，转成 Windows 风格
  local contentPath; contentPath="$(cygpath -w "$f" 2>/dev/null || echo "$f")"
  curl -s -X POST "$api" \
    --data-urlencode "dataId=$did" \
    --data-urlencode "$gp=$grp" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content@$contentPath"
}

for f in "$DIR"/*.yml; do
  dataId="$(basename "$f")"
  resp="$(publish "http://$NACOS/nacos/v3/admin/cs/config" "$dataId" groupName "$GROUP" "$f")"
  if printf '%s' "$resp" | grep -q '"code":0'; then
    echo "导入 $dataId -> $NACOS ($GROUP) [v3]"
  else
    # 回退旧版 v1 API（Nacos 2.x 或未下线 v1 的 3.x）
    resp="$(publish "http://$NACOS/nacos/v1/cs/configs" "$dataId" group "$GROUP" "$f")"
    echo "导入 $dataId -> $NACOS ($GROUP) [v1 回退] $resp"
  fi
done
echo "完成。可在控制台查看（Nacos 3.x 控制台端口 8080：http://$NACOS/nacos → 或 :8080/nacos）。"
