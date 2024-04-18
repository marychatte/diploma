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

    # Kill all java processes, run the server and run wrk to warm up the server
    killall -9 java
    java -jar ./build/libs/diploma-1.0-SNAPSHOT.jar server $server_type &
    sleep 5
    wrk -c1000 -t8 -d5s -s ./wrk/script.lua --timeout 1000 http://localhost:12345/
    #

    t=0
    if [[ "$c" -gt 8 ]]; then
      t=8
    else
      t=$c
    fi
    wrk -c$c -t$t -d10s -s ./wrk/script.lua --latency --timeout 1000 http://localhost:12345/ >> ${result_file}
    printf '\n' >> ${result_file}
  done
done

killall -9 java