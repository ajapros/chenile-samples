#!/usr/bin/env sh
set -eu

echo "execution_id=${CHENILE_SCHEDULER_EXECUTION_ID:-}"
echo "job_name=${CHENILE_SCHEDULER_JOB_NAME:-}"
echo "worker=${CHENILE_SCHEDULER_WORKER:-}"
echo "service=${CHENILE_SCHEDULER_SERVICE_NAME:-}"
echo "operation=${CHENILE_SCHEDULER_OPERATION_NAME:-}"
echo "payload=${CHENILE_SCHEDULER_PAYLOAD:-}"

# Replace this script with the real worker implementation.
# Production workers should be idempotent for the same execution id.
