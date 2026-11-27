#!/usr/bin/env bash
# xfcy 本地服务统一管理（自建 mvn 实例，不用 IDE 托管进程）
#
# 用法：
#   bash deploy/scripts/restart.sh status                列出全部服务端口/PID/主类
#   bash deploy/scripts/restart.sh stop   <name>         按端口杀实例
#   bash deploy/scripts/restart.sh start  <name>         后台起 mvn 实例，日志落 deploy/run/<name>.log
#   bash deploy/scripts/restart.sh restart <name>        先 stop 再 start
#   bash deploy/scripts/restart.sh log    <name>         跟踪日志
#
# name 取值：gateway store order operator shop es job mq cash enterprise operator-web microapp
# （operator = provider 9993，operator-web = BFF 8083）

set -u
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
RUN_DIR="$ROOT/deploy/run"
mkdir -p "$RUN_DIR"

# name|module|port|env（env 为启动时注入的环境变量，留空表示无）
# job 的 executor 端口固定 29999：9999 已被本机其他项目占用，勿改回默认
SERVICES=(
  "gateway|xfcy-gateway|18080|"
  "store|xfcy-service/xfcy-store|9991|"
  "order|xfcy-service/xfcy-order|9992|"
  "operator|xfcy-service/xfcy-operator|9993|"
  "shop|xfcy-service/xfcy-shop|9994|"
  "es|xfcy-service/xfcy-es|9995|"
  "job|xfcy-service/xfcy-service-job|8001|XXL_JOB_EXECUTOR_PORT=29999"
  "mq|xfcy-service/xfcy-service-mq|8089|"
  "cash|xfcy-web/xfcy-cash-web|8085|"
  "enterprise|xfcy-web/xfcy-enterprise-web|8081|"
  "operator-web|xfcy-web/xfcy-operator-web|8083|"
  "microapp|xfcy-web/xfcy-microapp-web|8087|"
)

find_service() {
  local name="$1"
  for row in "${SERVICES[@]}"; do
    IFS='|' read -r n m p e <<<"$row"
    if [ "$n" = "$name" ]; then
      printf '%s|%s|%s' "$m" "$p" "$e"
      return 0
    fi
  done
  return 1
}

pid_on_port() {
  netstat -ano | grep LISTENING | grep -E "[:.]${1}[[:space:]]" | awk '{print $NF}' | head -1
}

cmd_status() {
  printf '%-14s %-6s %-8s %s\n' NAME PORT PID MAINCLASS
  for row in "${SERVICES[@]}"; do
    IFS='|' read -r n m p e <<<"$row"
    pid="$(pid_on_port "$p")"
    cls=""
    if [ -n "$pid" ]; then
      cls="$(powershell -NoProfile -Command "(Get-CimInstance Win32_Process -Filter \"ProcessId=$pid\").CommandLine" 2>/dev/null | grep -oE 'com\.restkeeper\.[A-Za-z.]+' | head -1)"
    fi
    printf '%-14s %-6s %-8s %s\n' "$n" "$p" "${pid:--}" "${cls:--}"
  done
}

cmd_stop() {
  local svc; svc="$(find_service "$1")" || { echo "未知服务：$1"; exit 1; }
  IFS='|' read -r module port envstr <<<"$svc"
  local pid; pid="$(pid_on_port "$port")"
  if [ -z "$pid" ]; then echo "$1($port) 未运行"; return 0; fi
  echo "kill $1($port) pid=$pid"
  taskkill //F //PID "$pid" >/dev/null 2>&1 || taskkill /F /PID "$pid" >/dev/null 2>&1
}

cmd_start() {
  local svc; svc="$(find_service "$1")" || { echo "未知服务：$1"; exit 1; }
  IFS='|' read -r module port envstr <<<"$svc"
  local pid; pid="$(pid_on_port "$port")"
  if [ -n "$pid" ]; then echo "$1($port) 已在运行 pid=$pid，先 stop"; exit 1; fi
  cd "$ROOT" || exit 1
  # shellcheck disable=SC2086
  nohup env $envstr mvn -pl "$module" -Dmaven.test.skip=true spring-boot:run >"$RUN_DIR/$1.log" 2>&1 &
  echo "started $1($port) -> $RUN_DIR/$1.log (mvn pid=$!)"
}

case "${1:-status}" in
  status) cmd_status ;;
  stop)   cmd_stop "$2" ;;
  start)  cmd_start "$2" ;;
  restart) cmd_stop "$2"; sleep 2; cmd_start "$2" ;;
  log)    tail -f "$RUN_DIR/$2.log" ;;
  *) echo "用法: bash deploy/scripts/restart.sh {status|stop|start|restart|log} <name>"; exit 1 ;;
esac
