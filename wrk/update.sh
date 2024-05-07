#!/bin/bash

sudo ulimit -n 500000

sudo launchctl limit maxfiles 500000 200000

sudo sysctl -w kern.ipc.somaxconn=500000
sudo sysctl -w kern.maxfiles=500000
sudo sysctl -w kern.maxfilesperproc=500000

sudo bash -c "echo 'kern.ipc.somaxconn=500000' >> /etc/sysctl.conf"
sudo bash -c "echo 'kern.maxfiles=500000' >> /etc/sysctl.conf"
sudo bash -c "echo 'kern.maxfilesperproc=500000' >> /etc/sysctl.conf"
