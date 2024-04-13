#!/bin/bash
server_type="reactor"
result_file="./results/wrk_${server_type}.txt"
count_connections=(1 10 100 )
#count_connections=(1)

printf "Server type: %s\n\n" "${server_type}" > ${result_file}
for c in "${count_connections[@]}"; do
  t=0
  if [[ "$c" -gt 8 ]]; then
    t=8
  else
    t=$c
  fi
  wrk -c$c -t$t -d10s -s ./wrk/script.lua --latency http://localhost:12345/ >> ${result_file}
  printf '\n' >> ${result_file}
done
