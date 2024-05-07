#!/bin/bash
count_connections=(1 2 4 6 8 10 20 40 50 70 90 100 200 300 400 500 700 900)
for ((i = 1000; i < 10000; i += 200)); do
    count_connections+=("$i")
done
for ((i = 10000; i <= 30000; i += 500)); do
    count_connections+=("$i")
done

bash ./wrk/wrk_script.sh $1 "local_from_1_to_3000" "_22_04" "${count_connections[@]}"