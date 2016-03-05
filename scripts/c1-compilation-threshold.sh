#!/bin/bash

THRESHOLD="200"

if [ "$1" != "" ]
then
    THRESHOLD="$1"
fi

java -cp build/libs/jvm-warmup-talk-0.0.1.jar -XX:+PrintCompilation -XX:Tier3CompileThreshold=2000 -XX:Tier3MinInvocationThreshold=100 -XX:Tier3InvocationThreshold=200 com.epickrram.talk.warmup.example.threshold.C1CompilationThresholdMain  | grep -E "(LOG|epickrram)"

