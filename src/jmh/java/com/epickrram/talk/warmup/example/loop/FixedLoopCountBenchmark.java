package com.epickrram.talk.warmup.example.loop;

import org.openjdk.jmh.annotations.*;

public class FixedLoopCountBenchmark
{
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public long fixedLoopCount()
    {
        return FixedLoopCount.doLoop();
    }
}