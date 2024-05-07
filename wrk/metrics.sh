#!/bin/bash

server_types=("asm" "eventgroup" "reactor" "netty")

## Original settings
count_connections=(1000 3000 10000 30000 100000 300000)
count_threads=(2 8 32 128)
count_iterations=3
test_time=30
body_sizes=(0 512 4096)

for server_type in "${server_types[@]}"; do
  dir_server_type=$server_type
  rm -r "$dir_server_type"
  mkdir "$dir_server_type"

  for body_size in "${body_sizes[@]}"; do
    dir_body_size="$dir_server_type/$body_size"
    mkdir "$dir_body_size"

    for count_thread in "${count_threads[@]}"; do
      dir_count_threads="$dir_body_size/$count_thread"
      mkdir "$dir_count_threads"

      for count_connection in "${count_connections[@]}"; do
        dir_count_connections="$dir_count_threads/$count_connection"
        mkdir "$dir_count_connections"

        count_ports=$((count_connection/45000))
        if [[ $((count_connection%45000)) -ne 0 ]]; then
          count_ports=$((count_ports + 1))
        fi
        connections_per_wrk=$((count_connection/count_ports))

        for i in $(seq 1 $count_iterations); do
          echo "-----------------------------------------------------------------"
          echo "Server: $server_type, Body size: $body_size, Thread: $count_thread, Connections: $count_connection, Iteration: $i, Ports: $count_ports, Connections per wrk: $connections_per_wrk"

  #       run server on another machine
          count_thread_selector=1
          count_thread_actor=1
          if [[ "$server_type" == "blocking" ]] || [[ "$server_type" == "reactor" ]] || [[ "$server_type" == "asm" ]]; then
              count_thread_actor=$count_thread
          else
              count_thread_selector=$count_thread
          fi


          echo "Setting up server"

          # kill server and wait until port stop responding
          ssh root@server "cd marychatte && (./kill.sh >/dev/null 2>&1)"
          while telnet server 12345 >/dev/null 2<&1 </dev/null >/dev/null 2>&1; do :; done
          echo "Server killed"

          # run server and wait till port is listenable again
          server_start_command="cd marychatte && ./run.sh $server_type $count_thread_selector $count_thread_actor $body_size $count_ports"
          ssh root@server "$server_start_command"
          sleep 1
          until telnet server 12345 >/dev/null 2<&1 </dev/null >/dev/null 2>&1; do :; done
          sleep 1
          echo "New server started & reachable"

  # https://stackoverflow.com/questions/29142/getting-ssh-to-execute-a-command-in-the-background-on-target-machine
#          ssh root@server "cd marychatte; nohup ./run.sh $server_type $count_thread_selector $count_thread_actor $body_size $count_ports > /dev/null 2>&1 </dev/null &"
#          until telnet server 12345 >/dev/null 2<&1 </dev/null >/dev/null 2>&1; do :; done

  #       warm up server
          echo "Start warmup"
          base_port=12345
          export WRK_BODY_SIZE=$body_size
          for (( j=0 ; j<$count_ports ; j++ )); do
            port=$((base_port + j))
            host="http://server:$port/"
            wrk -c1000 -t2 -d10s -s ./wrk/script.lua --timeout 10000 $host &
          done
          wait

  #       run wrk on this machine
          echo "Start benchmark"
          base_file_name="${server_type}_${body_size}_${count_thread}_${count_connection}"
          ./parallel.sh $body_size $count_ports $connections_per_wrk $test_time "$dir_count_connections/$base_file_name"
          echo "Benchmark finished"
        done
      done
    done
  done
done

ssh root@server "cd marychatte && (./kill.sh >/dev/null 2>&1)"
