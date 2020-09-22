#!/usr/bin/env bash

set -e
COMMAND=$(./build.sh)

$COMMAND com.awslabs.iot_all_the_things.special_projects_edition.totally_lit.hue.HueRainbow "$@"
