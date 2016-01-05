package com.epickrram.talk.warmup.example.loop;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;

public class FixedLoopCountBenchmark
{
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public long fixedLoopCount10()
    {
        return FixedLoopCount.doLoop10();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public long fixedLoopCount100()
    {
        return FixedLoopCount.doLoop100();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public long fixedLoopCount1000()
    {
        return FixedLoopCount.doLoop1000();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public long fixedLoopCount10000()
    {
        return FixedLoopCount.doLoop10000();
    }
}