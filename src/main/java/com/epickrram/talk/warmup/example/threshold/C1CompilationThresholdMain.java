package com.epickrram.talk.warmup.example.threshold;

import com.epickrram.talk.warmup.example.log.StdoutLogger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public final class C1CompilationThresholdMain
{
    public static void main(final String[] args)
    {
        final int invocationCount = 146;
        long dceGuard = wrapper(System.nanoTime(), getLoopCount(args), invocationCount);

        StdoutLogger.log("Pausing for a few seconds to make sure compile hasn't been triggered yet...%n");
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5L));

        StdoutLogger.log("About to perform invocation %d%n", invocationCount + 1);

        dceGuard += exerciseTier3CompilationThreshold(System.nanoTime(), 20);

        if(dceGuard == 9283749238473294L)
        {
            System.err.println("Well, this is unexpected");
        }

        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5L));
    }

    private static int getLoopCount(final String[] args)
    {
        if(args.length != 0)
        {
            return Integer.parseInt(args[0]);
        }

        return 21;
    }

    private static long wrapper(final long input, final int loopCount, int invocationCount)
    {
        StdoutLogger.log("Loop count is: %d%n", loopCount);

        long accumulator = 0L;
        for(int i = 0; i < invocationCount; i++)
        {
            accumulator += exerciseTier3CompilationThreshold(input, loopCount);
            StdoutLogger.log("Finished invocation: %d, back-edge count should be %d%n", i + 1, (i + 1) * loopCount);
        }
        return accumulator;
    }

    private static long exerciseTier3CompilationThreshold(final long input, int loopCount)
    {
        long value = input;
        for(int i = 0; i < loopCount; i++)
        {
            value =  17L * input * value;
        }

        return value;
    }

}
