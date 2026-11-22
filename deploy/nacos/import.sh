#!/usr/bin/env bash
# 将 deploy/nacos/*.yml 导入本地 Nacos（先启动 deploy/docker-compose.yml）
set -euo pipefail
NACOS="${NACOS_SERVER:-127.0.0.1:8848}"
GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
DIR="$(cd "$(dirname "$0")" && pwd)"
for f in "$DIR"/nacos/*.yml; do
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
