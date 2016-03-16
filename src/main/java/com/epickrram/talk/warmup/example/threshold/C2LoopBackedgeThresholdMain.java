package com.epickrram.talk.warmup.example.threshold;

import com.epickrram.talk.warmup.example.log.StdoutLogger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public final class C2LoopBackedgeThresholdMain
{
    public static void main(final String[] args)
    {
        long dceGuard = wrapper(System.nanoTime(), getLoopCount(args));

        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5L));

        if(dceGuard == 9283749238473294L)
        {
            System.err.println("Well, this is unexpected");
        }
    }

    private static int getLoopCount(final String[] args)
    {
        if(args.length != 0)
        {
            return Integer.parseInt(args[0]);
        }

        return 60000;
    }

    private static long wrapper(final long input, final int loopCount)
    {
        StdoutLogger.log("Loop count is: %d%n", loopCount);
        return exerciseServerLoopBackedgeThreshold(input, loopCount);
    }

    private static long exerciseServerLoopBackedgeThreshold(final long input, final int loopCount)
    {
        long value = input;
        for(int i = 0; i < loopCount; i++)
        {
            value = 17L * value;
        }

        return value;
    }
}