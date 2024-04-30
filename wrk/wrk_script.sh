#!/bin/bash

server_type=$1
dir=$2
suffix=$3
result_file="./results/$dir/wrk_${server_type}_$suffix.txt"
count_connections=("$@")
count_connections=("${count_connections[@]:3}")
count_iterations=5
#host="http://209.38.248.52:12345/"
host="http://localhost:12345/"

printf "Server type: %s\n\n" "${server_type}" > ${result_file}
for c in "${count_connections[@]}"; do
  for i in $(seq 1 $count_iterations); do
    echo "Connections: $c, Iteration: $i"

#    command="bash ~/run.sh $server_type &"
#    echo "Running command: $command"
#    ssh -t root@209.38.248.52 -t $command >> /dev/null
    kill -9 $(lsof -t -i:12345)
    sleep 2
    java -Xmx6g -jar ./build/libs/diploma-1.0-SNAPSHOT.jar server $server_type &
    sleep 5

    wrk -c1000 -t8 -d10s -s ./wrk/script.lua --timeout 10000 $host

    t=0
    if [[ "$c" -gt 8 ]]; then
      t=8
    else
      t=$c
    fi
    wrk -c$c -t$t -d10s -s ./wrk/script.lua --latency --timeout 100000 $host >> ${result_file}
    printf '\n' >> ${result_file}
  done
done
