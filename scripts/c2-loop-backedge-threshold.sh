#!/bin/bash

THRESHOLD="10000"

COMPILE_THRESHOLD="-XX:CompileThreshold=5000"

if [ "$1" != "" ]
then
    THRESHOLD="$1"
fi

java -cp build/libs/jvm-warmup-talk-0.0.1.jar -XX:+TieredCompilation  -XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation -XX:+PrintCompilation $COMPILE_THRESHOLD com.epickrram.talk.warmup.example.threshold.C2LoopBackedgeThresholdMain $THRESHOLD | grep -E "(LOG|epickrram)"
