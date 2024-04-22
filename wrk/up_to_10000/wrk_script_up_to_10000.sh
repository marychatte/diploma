#!/bin/bash
count_connections=(1 10 50 100 200 300 400 500 1000 2000 3000 4000 5000 6000 7000 8000 9000 10000)

bash ./wrk/wrk_script.sh $1 "up_to_10000" "up_to_10000_4" "${count_connections[@]}"