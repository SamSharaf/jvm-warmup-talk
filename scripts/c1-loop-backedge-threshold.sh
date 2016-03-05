#!/bin/bash

THRESHOLD="60416"

if [ "$1" != "" ]
then
    THRESHOLD="$1"
fi

java -cp build/libs/jvm-warmup-talk-0.0.1.jar -XX:+PrintCompilation -XX:Tier3BackEdgeThreshold=60000 com.epickrram.talk.warmup.example.threshold.C1LoopBackedgeThresholdMain $THRESHOLD | grep -E "(LOG|epickrram)"

