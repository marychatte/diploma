#!/bin/bash
server_type="blocking"
count_connections=(1 2 3 4 5 6 7 8 9 10 20 30 40 50 60 70 80 90 100 200 300 400 500 600 700 800 900 1000 2000 3000 4000 5000 6000 7000 8000 9000 10000)

bash ./wrk/wrk_script.sh $server_type "${count_connections[@]}"