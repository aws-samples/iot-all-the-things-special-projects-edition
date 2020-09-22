#!/usr/bin/env bash

if ! command -v npm &> /dev/null
then
  >&2 echo "npm not detected. Please install npm and re-run this script. If you prefer to run everything in Docker simply run the ./deploy.sh script."
  exit 1
fi

npm install serverless
npm install --save-dev serverless-apigw-binary
