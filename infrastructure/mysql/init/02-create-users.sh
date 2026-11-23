#!/bin/bash
set -e

mysql -u root -p"${MYSQL_ROOT_PASSWORD}" <<-EOSQL
  -- Consolidated Backend User & Database
  CREATE USER IF NOT EXISTS '${DB_USER:-smartpay}'@'%' IDENTIFIED BY '${DB_PASSWORD:-changeme}';
  GRANT ALL PRIVILEGES ON ${DB_NAME:-smartpay}.* TO '${DB_USER:-smartpay}'@'%';

  -- Auth Service
  CREATE USER IF NOT EXISTS '${AUTH_DB_USER:-auth_service}'@'%' IDENTIFIED BY '${AUTH_DB_PASSWORD:-changeme_auth}';
  GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
    ON ${AUTH_DB_NAME:-smartpay_auth}.* TO '${AUTH_DB_USER:-auth_service}'@'%';

  -- User Service
  CREATE USER IF NOT EXISTS '${USER_DB_USER:-user_service}'@'%' IDENTIFIED BY '${USER_DB_PASSWORD:-changeme_user}';
  GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
    ON ${USER_DB_NAME:-smartpay_users}.* TO '${USER_DB_USER:-user_service}'@'%';

  -- Wallet Service
  CREATE USER IF NOT EXISTS '${WALLET_DB_USER:-wallet_service}'@'%' IDENTIFIED BY '${WALLET_DB_PASSWORD:-changeme_wallet}';
  GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
    ON ${WALLET_DB_NAME:-smartpay_wallets}.* TO '${WALLET_DB_USER:-wallet_service}'@'%';

  -- Transaction Service
  CREATE USER IF NOT EXISTS '${TRANSACTION_DB_USER:-transaction_service}'@'%' IDENTIFIED BY '${TRANSACTION_DB_PASSWORD:-changeme_transaction}';
  GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
    ON ${TRANSACTION_DB_NAME:-smartpay_transactions}.* TO '${TRANSACTION_DB_USER:-transaction_service}'@'%';

  -- Payment Service
  CREATE USER IF NOT EXISTS '${PAYMENT_DB_USER:-payment_service}'@'%' IDENTIFIED BY '${PAYMENT_DB_PASSWORD:-changeme_payment}';
  GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
    ON ${PAYMENT_DB_NAME:-smartpay_payments}.* TO '${PAYMENT_DB_USER:-payment_service}'@'%';

  -- Beneficiary Service
  CREATE USER IF NOT EXISTS '${BENEFICIARY_DB_USER:-beneficiary_service}'@'%' IDENTIFIED BY '${BENEFICIARY_DB_PASSWORD:-changeme_beneficiary}';
  GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
    ON ${BENEFICIARY_DB_NAME:-smartpay_beneficiaries}.* TO '${BENEFICIARY_DB_USER:-beneficiary_service}'@'%';

  -- Notification Service
  CREATE USER IF NOT EXISTS '${NOTIFICATION_DB_USER:-notification_service}'@'%' IDENTIFIED BY '${NOTIFICATION_DB_PASSWORD:-changeme_notification}';
  GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
    ON ${NOTIFICATION_DB_NAME:-smartpay_notifications}.* TO '${NOTIFICATION_DB_USER:-notification_service}'@'%';

  -- Audit Service
  CREATE USER IF NOT EXISTS '${AUDIT_DB_USER:-audit_service}'@'%' IDENTIFIED BY '${AUDIT_DB_PASSWORD:-changeme_audit}';
  GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES, CREATE VIEW, SHOW VIEW
    ON ${AUDIT_DB_NAME:-smartpay_audit}.* TO '${AUDIT_DB_USER:-audit_service}'@'%';

  FLUSH PRIVILEGES;
EOSQL
