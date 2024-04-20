#!/bin/bash
server_type=$1
result_file="./results/wrk_${server_type}.txt"
count_connections=("$@")
count_connections=("${count_connections[@]:1}")
count_iterations=5

printf "Server type: %s\n\n" "${server_type}" > ${result_file}
for c in "${count_connections[@]}"; do
  for i in $(seq 1 $count_iterations); do
    echo "Connections: $c, Iteration: $i"

    command="bash ~/run.sh $server_type &"
    ssh -t root@209.38.248.52 $command >> /dev/null
    sleep 5

    wrk -c1000 -t8 -d10s -s ./wrk/script.lua --timeout 10000 http://209.38.248.52:12345/

    t=0
    if [[ "$c" -gt 8 ]]; then
      t=8
    else
      t=$c
    fi
    wrk -c$c -t$t -d60s -s ./wrk/script.lua --latency --timeout 100000 http://209.38.248.52:12345/ >> ${result_file}
    printf '\n' >> ${result_file}
  done
done
