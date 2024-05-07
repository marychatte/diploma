#!/bin/bash

set -e
set -o pipefail

if [[ "$#" -ne 4 ]] && [[ "$#" -ne 5 ]]; then
    echo "Expected 4 (or 5) parameters:"
    echo " - Request body size in bytes"
    echo " - Number of ports opened on server"
    echo " - Number of connections per wrk client"
    echo " - Duration of the test in seconds"
    echo " - (Optional) Output template: 'dir/file_prefix' will result in output to 'dir/file_prefix_<port_number>.txt'"
    exit 1
fi

export WRK_BODY_SIZE=$1

nProc=0
if [[ $OSTYPE == *"linux"* ]]; then
  nProc=$(nproc --all)
else
  nProc=$(sysctl -n hw.ncpu)
fi;

nPorts=$2
nConn=$3
nSec=$4

if [[ $nProc -le $nPorts ]]; then
  echo "Number of ports exceeds number of CPU ($nProc)"
  exit 1
fi

threadsPerClient=$((nProc/nPorts))
totalThreads=$((threadsPerClient * nPorts))

# leave at least 2 CPU for OS
if [[ $totalThreads -gt $((nProc - 2)) ]]; then
  if [[ $nPorts -eq 1 ]]; then
    threadsPerClient=$((threadsPerClient-2))
  else
    threadsPerClient=$((threadsPerClient-1))
  fi;
fi;

if [[ $threadsPerClient -eq 0 ]]; then
  echo "Not enough CPU for specified number of clients"
  exit 1
fi

echo "Using $threadsPerClient CPUs per wrk process with total of $((threadsPerClient * nPorts)) CPUs"

base_port=12345

PIDs=()

mkdir -p "wrk/parallel"

outputTemplate=$5

for (( i=0 ; i<$nPorts ; i++ )); do
  port=$((base_port + i))
  host="http://${HOST_ADDRESS:-localhost}:$port/"

  output=''
  if [[ $# -eq 5 ]]; then
    output="${outputTemplate}_${port}.txt"
  else
    output=/dev/stdout
  fi;

  wrk -c$nConn -t$threadsPerClient -d$((nSec))s -s ./wrk/script.lua --timeout 1000 $host >> $output &
  PIDs+=($!)
  echo "Started wrk on $port port with pid $!"
done

trap "trap - SIGINT && kill -- -$$" SIGINT SIGTERM # kill background processes when parent dies

wait # wait for all background processes
