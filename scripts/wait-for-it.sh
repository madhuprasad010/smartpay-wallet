#!/usr/bin/env bash
# ============================================================
# wait-for-it.sh — Wait for a TCP host:port to become available
# Based on vishnubob/wait-for-it, simplified for SmartPay.
# Usage: ./wait-for-it.sh host:port [-t timeout] [-- command args]
# ============================================================

set -e

WAITFORIT_TIMEOUT=30
WAITFORIT_QUIET=0
WAITFORIT_HOST=""
WAITFORIT_PORT=""
WAITFORIT_CMD=""

usage() {
  echo "Usage: $0 host:port [-t timeout] [-q] [-- command args]"
  echo ""
  echo "  -t TIMEOUT   Timeout in seconds (default: 30, 0 = no timeout)"
  echo "  -q           Quiet mode"
  echo "  -- CMD ARGS  Execute command after the host:port is available"
  exit 1
}

wait_for() {
  if [ "$WAITFORIT_TIMEOUT" -gt 0 ]; then
    echo "Waiting $WAITFORIT_TIMEOUT seconds for $WAITFORIT_HOST:$WAITFORIT_PORT..."
  else
    echo "Waiting for $WAITFORIT_HOST:$WAITFORIT_PORT without timeout..."
  fi

  WAITFORIT_START_TS=$(date +%s)

  while true; do
    if nc -z "$WAITFORIT_HOST" "$WAITFORIT_PORT" >/dev/null 2>&1; then
      WAITFORIT_END_TS=$(date +%s)
      echo "$WAITFORIT_HOST:$WAITFORIT_PORT is available after $((WAITFORIT_END_TS - WAITFORIT_START_TS)) seconds"
      return 0
    fi

    if [ "$WAITFORIT_TIMEOUT" -gt 0 ]; then
      WAITFORIT_NOW_TS=$(date +%s)
      if [ $((WAITFORIT_NOW_TS - WAITFORIT_START_TS)) -ge "$WAITFORIT_TIMEOUT" ]; then
        echo "Timeout reached: $WAITFORIT_HOST:$WAITFORIT_PORT is still not available after $WAITFORIT_TIMEOUT seconds"
        return 1
      fi
    fi

    sleep 1
  done
}

# Parse hostport
if [ $# -eq 0 ]; then
  usage
fi

HOSTPORT="$1"
shift

# Split host:port
WAITFORIT_HOST="${HOSTPORT%%:*}"
WAITFORIT_PORT="${HOSTPORT##*:}"

if [ -z "$WAITFORIT_HOST" ] || [ -z "$WAITFORIT_PORT" ]; then
  echo "Error: invalid host:port format"
  usage
fi

# Parse remaining options
while [ $# -gt 0 ]; do
  case "$1" in
    -t)
      WAITFORIT_TIMEOUT="$2"
      shift 2
      ;;
    -q)
      WAITFORIT_QUIET=1
      shift
      ;;
    --)
      shift
      WAITFORIT_CMD="$*"
      break
      ;;
    *)
      echo "Unknown option: $1"
      usage
      ;;
  esac
done

if [ "$WAITFORIT_QUIET" -eq 1 ]; then
  wait_for >/dev/null 2>&1
else
  wait_for
fi

WAITFORIT_RESULT=$?

if [ -n "$WAITFORIT_CMD" ]; then
  if [ $WAITFORIT_RESULT -eq 0 ]; then
    exec $WAITFORIT_CMD
  else
    echo "Failed to connect, not executing command"
    exit $WAITFORIT_RESULT
  fi
fi

exit $WAITFORIT_RESULT
