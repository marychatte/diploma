#!/bin/bash

bash ./wrk/after_10000/wrk_script_eventloop.sh
bash ./wrk/after_10000/wrk_script_ktor.sh
bash ./wrk/after_10000/wrk_script_asm.sh
bash ./wrk/after_10000/wrk_script_reactor.sh
bash ./wrk/up_to_10000/wrk_script_blocking.sh
bash ./wrk/after_10000/wrk_script_netty.sh

