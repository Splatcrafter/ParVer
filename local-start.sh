#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
java -Dspring.profiles.active=dev -jar parver-backend/target/parver-backend-0.1.0.jar
