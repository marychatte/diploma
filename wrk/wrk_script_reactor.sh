#!/bin/bash
server_type="reactor"
count_connections=(1 10 100 1000 10000 100000)

bash ./wrk/wrk_script.sh $server_type "${count_connections[@]}"
