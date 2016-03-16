#!/bin/bash

THRESHOLD=200

if [ "$1" != "" ]
then
    THRESHOLD="$1"
fi

java -cp build/libs/jvm-warmup-talk-0.0.1.jar -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:+TieredCompilation -XX:MaxInlineSize=9 -XX:+PrintInlining com.epickrram.talk.warmup.example.threshold.InliningThresholdMain $THRESHOLD
