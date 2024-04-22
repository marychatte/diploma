#!/bin/bash
count_connections=(20000 30000 40000 50000 70000 100000 150000 200000 500000)

bash ./wrk/wrk_script.sh $1 "after_10000" "after_10000" "${count_connections[@]}"