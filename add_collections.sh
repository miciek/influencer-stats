#!/bin/bash

set -e

for i in {1..250}; do
  curl --silent --output /dev/null -XPUT -H 'Content-Type: application/json' "http://localhost:8080/collections/$i" -d '{ "videos": [] }'
done
