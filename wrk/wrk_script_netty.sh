#!/bin/bash
server_type="netty"
count_connections=(1 10 100 300 500 700 1000 1500 2000 4000 6000 8000 10000 15000 20000 25000 30000 40000 50000 70000 90000 100000 200000 300000)

bash ./wrk/wrk_script.sh $server_type "${count_connections[@]}"
