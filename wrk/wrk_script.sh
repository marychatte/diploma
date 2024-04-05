#!/bin/bash
server_type="blocking"
result_file="./results/wrk_${server_type}.txt"
count_connections=(1 2 3 4 5 6 7 8 9 10 20 30 40 50 60 70 80 90 100 200 300 400 500 600 700 800 900 1000 2000 3000 4000 5000 6000 7000 8000 9000 10000)

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
