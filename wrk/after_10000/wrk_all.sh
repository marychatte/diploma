#!/bin/bash

sh ./wrk/after_10000/wrk_script_eventloop.sh
sh ./wrk/after_10000/wrk_script_ktor.sh
sh ./wrk/after_10000/wrk_script_netty.sh
sh ./wrk/after_10000/wrk_script_reactor.sh
