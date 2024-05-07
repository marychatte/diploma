#!/bin/zsh

if ! ./gradlew fatJar; then
  exit 1
fi

scp -r wrk/ root@10.0.2.2:~/marychatte/
scp build/libs/diploma-1.0-SNAPSHOT.jar root@10.0.2.3:~/marychatte/
