#!/usr/bin/env bash
# 将 deploy/nacos-vm/*.yml 导入 VM（192.168.20.88）上的 Nacos。
# 与 deploy/nacos/import.sh 的区别：本目录为 VM 专用派生版（地址 192.168.20.88、凭据同手册 §3）。
set -euo pipefail
NACOS="${NACOS_SERVER:-192.168.20.88:8848}"
GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
DIR="$(cd "$(dirname "$0")" && pwd)"

publish() { # $1=apiBase $2=dataId $3=groupParamName $4=group $5=file
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
    resp="$(publish "http://$NACOS/nacos/v1/cs/configs" "$dataId" group "$GROUP" "$f")"
    echo "导入 $dataId -> $NACOS ($GROUP) [v1 回退] $resp"
  fi
done
echo "完成。可在控制台查看（Nacos 3.x 控制台端口 8080：http://$NACOS/nacos → 或 :8080/nacos）。"
