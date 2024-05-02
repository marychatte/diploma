#!/bin/bash

#killall -9 java
#java -XX:-MaxFDLimit -jar ./build/libs/diploma-1.0-SNAPSHOT.jar server eventloop &
#sleep 5
wrk -c1000 -t8 -d1s -s ./wrk/script.lua http://localhost:12345/

#server_type="blocking"
#command="bash ~/run.sh $server_type"
#ssh -t root@209.38.248.52 $command >> /dev/null


#wrk -c1000 -t1 -d1s -s ./wrk/script.lua  http://localhost:12345/
#wrk -c200 -t1 -d1s -s ./wrk/script.lua  http://209.38.248.52:12345/
#wrk -c1000 -t8 -d10s -s ./wrk/script.lua  http://localhost:12345/
#wrk -c1000 -t8 -d10s -s ./wrk/script.lua --timeout 1000 http://209.38.248.52:12345/
#wrk -c5000 -t8 -d5s -s ./wrk/script.lua  http://localhost:12345/
#wrk -c1000 -t1 -d5s -s ./wrk/script.lua  http://localhost:12345/
#wrk -c1 -t1 -d1s -s ./wrk/script.lua --timeout 1000 http://localhost:12345/


## login to the server
# ssh root@209.38.248.52
#
## force update repo and produce fatJar
# ~/update.sh
#
## setup limits and run server
# bash ~/run.sh netty
