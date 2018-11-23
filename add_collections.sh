#!/bin/bash
for i in {1..100}; do
  curl -XPUT -H 'Content-Type: application/json' "http://localhost:8080/collections/$i" -d '{ "videos": [] }'
done
