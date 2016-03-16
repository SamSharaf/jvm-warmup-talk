#!/bin/bash

THRESHOLD="200"

if [ "$1" != "" ]
then
    THRESHOLD="$1"
fi

java -cp build/libs/jvm-warmup-talk-0.0.1.jar -XX:-TieredCompilation  -XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation -XX:+PrintCompilation -XX:CompileThreshold=200 com.epickrram.talk.warmup.example.threshold.C2InvocationThresholdMain $THRESHOLD | grep -E "(LOG|epickrram)"

