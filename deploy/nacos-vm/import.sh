#!/usr/bin/env bash
# 将 deploy/nacos-vm/*.yml 导入 VM（192.168.20.88）上的 Nacos。
# 与 deploy/nacos/import.sh 的区别：本目录为 VM 专用派生版（地址 192.168.20.88、MySQL/Redis 密码 123456）。
set -euo pipefail
NACOS="${NACOS_SERVER:-192.168.20.88:8848}"
GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
DIR="$(cd "$(dirname "$0")" && pwd)"
for f in "$DIR"/*.yml; do
  dataId="$(basename "$f")"
  echo "导入 $dataId -> $NACOS ($GROUP)"
  curl -s -X POST "http://$NACOS/nacos/v1/cs/configs" \
    --data-urlencode "dataId=$dataId" \
    --data-urlencode "group=$GROUP" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content@$f"
  echo
done
echo "完成。可在控制台 http://$NACOS/nacos 查看。"
