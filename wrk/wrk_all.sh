#!/bin/bash

sh ./wrk/wrk_script_blocking.sh
sh ./wrk/wrk_script_netty.sh
sh ./wrk/wrk_script_eventloop.sh
sh ./wrk/wrk_script_reactor.sh
