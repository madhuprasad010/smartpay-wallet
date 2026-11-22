#!/usr/bin/env bash
# ============================================================
# SmartPay Wallet — Health Check Script
# Checks all service health endpoints and reports status.
# ============================================================

set -euo pipefail

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

BASE_URL="${BASE_URL:-http://localhost}"
TIMEOUT="${HEALTH_CHECK_TIMEOUT:-5}"

TOTAL=0
HEALTHY=0
UNHEALTHY=0

echo "=============================================="
echo "  SmartPay Wallet — Health Check"
echo "  $(date '+%Y-%m-%d %H:%M:%S')"
echo "=============================================="
echo ""

check_service() {
  local SERVICE="$1"
  local URL="$2"
  TOTAL=$((TOTAL + 1))

  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout "$TIMEOUT" --max-time "$TIMEOUT" "$URL" 2>/dev/null || echo "000")

  if [ "$HTTP_CODE" -ge 200 ] && [ "$HTTP_CODE" -lt 300 ]; then
    printf "  ${GREEN}✓${NC} %-25s [HTTP %s]\n" "$SERVICE" "$HTTP_CODE"
    HEALTHY=$((HEALTHY + 1))
  else
    printf "  ${RED}✗${NC} %-25s [HTTP %s]\n" "$SERVICE" "$HTTP_CODE"
    UNHEALTHY=$((UNHEALTHY + 1))
  fi
}

check_service "Nginx" "${BASE_URL}/nginx-health"
check_service "API Gateway" "${BASE_URL}:8080/actuator/health"
check_service "Auth Service" "${BASE_URL}:8081/actuator/health"
check_service "User Service" "${BASE_URL}:8082/actuator/health"
check_service "Wallet Service" "${BASE_URL}:8083/actuator/health"
check_service "Transaction Service" "${BASE_URL}:8084/actuator/health"
check_service "Payment Service" "${BASE_URL}:8085/actuator/health"
check_service "Beneficiary Service" "${BASE_URL}:8086/actuator/health"
check_service "Notification Service" "${BASE_URL}:8087/actuator/health"
check_service "Audit Service" "${BASE_URL}:8088/actuator/health"

echo ""
echo "----------------------------------------------"
printf "  Total: %d | ${GREEN}Healthy: %d${NC} | ${RED}Unhealthy: %d${NC}\n" "$TOTAL" "$HEALTHY" "$UNHEALTHY"
echo "----------------------------------------------"

if [ "$UNHEALTHY" -gt 0 ]; then
  echo -e "\n${RED}Some services are unhealthy!${NC}"
  exit 1
else
  echo -e "\n${GREEN}All services are healthy!${NC}"
  exit 0
fi
