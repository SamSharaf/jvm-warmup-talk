package com.epickrram.talk.warmup.example.threshold;

import com.epickrram.talk.warmup.example.log.StdoutLogger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public final class C2InvocationThresholdMain
{
    public static void main(final String[] args)
    {
        long dceGuard = wrapper(System.nanoTime(), getLoopCount(args));

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

        return 256;
    }

    private static long wrapper(final long input, final int loopCount)
    {
        StdoutLogger.log("Loop count is: %d%n", loopCount);
        long accumulator = 0L;
        for(int i = 0; i < loopCount; i++)
        {
            accumulator += exerciseServerCompileThreshold(input);
        }
        return accumulator;
    }

    private static long exerciseServerCompileThreshold(final long input)
    {
        return 17L * input;
    }

}
