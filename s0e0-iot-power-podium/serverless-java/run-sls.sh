#!/usr/bin/env bash

set -e

DEPLOY_TAG="s0e0-iot-power-podium-deploy"
LOCAL_SLS="./node_modules/.bin/sls"

if [ ! -f "$LOCAL_SLS" ];
then
  >&2 echo "Serverless framework installation not detected, using Docker"
  AWS_CLI_ERROR_MESSAGE_PREFIX="No"
  AWS_CLI_ERROR_MESSAGE_SUFFIX="found via aws configure get, do you have the AWS CLI configured on this system? This command does NOT retrieve credentials from EC2 instance metadata."

  # Allow failures, we will catch them
  set +e

  if [ ! command -v aws &> /dev/null ]; then
    echo "AWS CLI must be installed and configured for deployments"
    exit 1
  fi

  # Is the AWS CLI configured?
  AWS_ACCESS_KEY_ID=$(aws configure get aws_access_key_id)

  if [ $? -ne 0 ]; then
    echo $AWS_CLI_ERROR_MESSAGE_PREFIX access key ID $AWS_CLI_ERROR_MESSAGE_SUFFIX
    exit 1
  fi

  AWS_SECRET_ACCESS_KEY=$(aws configure get aws_secret_access_key)

  if [ $? -ne 0 ]; then
    echo $AWS_CLI_ERROR_MESSAGE_PREFIX secret access key $AWS_CLI_ERROR_MESSAGE_SUFFIX
    exit 1
  fi

  REGION=$(aws configure get region)

  if [ $? -ne 0 ]; then
    echo $AWS_CLI_ERROR_MESSAGE_PREFIX region $AWS_CLI_ERROR_MESSAGE_SUFFIX
    exit 1
  fi

  time docker build -t $DEPLOY_TAG . -f Dockerfile.deploy

  docker run \
    -i --rm \
    -e AWS_REGION=$REGION \
    -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID \
    -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY \
    $DEPLOY_TAG $LOCAL_SLS $@

  exit 0
else
  >&2 echo "Serverless framework installation detected, running natively"
  $LOCAL_SLS $@

  exit 0
fi
