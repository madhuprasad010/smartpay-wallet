#!/usr/bin/env bash
# ============================================================
# SmartPay Wallet — Kafka Topic Initialization (KRaft mode)
# Run inside the Kafka container after the broker is ready.
# ============================================================

set -euo pipefail

BOOTSTRAP="${KAFKA_BOOTSTRAP_SERVERS:-localhost:29092}"
PARTITIONS="${KAFKA_DEFAULT_PARTITIONS:-3}"
REPLICATION="${KAFKA_DEFAULT_REPLICATION:-1}"

# Determine the correct kafka-topics command path
if command -v kafka-topics &>/dev/null; then
  KAFKA_TOPICS="kafka-topics"
elif [ -f /opt/kafka/bin/kafka-topics.sh ]; then
  KAFKA_TOPICS="/opt/kafka/bin/kafka-topics.sh"
elif [ -f /usr/bin/kafka-topics ]; then
  KAFKA_TOPICS="/usr/bin/kafka-topics"
else
  echo "ERROR: kafka-topics command not found"
  exit 1
fi

echo "==> Waiting for Kafka broker at ${BOOTSTRAP}..."
until $KAFKA_TOPICS --bootstrap-server "$BOOTSTRAP" --list &>/dev/null; do
  echo "    Kafka not ready yet, retrying in 3s..."
  sleep 3
done
echo "==> Kafka broker is ready."

# --- Primary event topics ---
TOPICS=(
  "smartpay.user.events"
  "smartpay.wallet.events"
  "smartpay.transaction.events"
  "smartpay.payment.events"
  "smartpay.audit.events"
)

for TOPIC in "${TOPICS[@]}"; do
  echo "==> Creating topic: ${TOPIC}"
  $KAFKA_TOPICS \
    --bootstrap-server "$BOOTSTRAP" \
    --create \
    --if-not-exists \
    --topic "$TOPIC" \
    --partitions "$PARTITIONS" \
    --replication-factor "$REPLICATION" \
    --config retention.ms=604800000 \
    --config cleanup.policy=delete
done

# --- Dead Letter Topics (DLT) ---
DLT_TOPICS=(
  "smartpay.user.events.dlt"
  "smartpay.wallet.events.dlt"
  "smartpay.transaction.events.dlt"
  "smartpay.payment.events.dlt"
  "smartpay.audit.events.dlt"
)

for TOPIC in "${DLT_TOPICS[@]}"; do
  echo "==> Creating DLT topic: ${TOPIC}"
  $KAFKA_TOPICS \
    --bootstrap-server "$BOOTSTRAP" \
    --create \
    --if-not-exists \
    --topic "$TOPIC" \
    --partitions 1 \
    --replication-factor "$REPLICATION" \
    --config retention.ms=2592000000 \
    --config cleanup.policy=delete
done

echo "==> All Kafka topics created successfully."
$KAFKA_TOPICS --bootstrap-server "$BOOTSTRAP" --list
