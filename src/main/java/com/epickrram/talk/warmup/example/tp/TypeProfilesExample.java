package com.epickrram.talk.warmup.example.tp;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * Example to demonstrate CHA by C2 compiler.
 *
 * Run with:
 *
 * -XX:+UnlockDiagnosticVMOptions -XX:+TraceClassLoading -XX:+TraceClassUnloading -XX:-TieredCompilation -XX:+LogCompilation -XX:CompileThreshold=20000
 */
public final class TypeProfilesExample
{
    private static final AtomicInteger ITERATION_COUNT = new AtomicInteger(0);
    public static volatile Calculator calculator;

    public static void main(final String[] args)
    {
        final Thread updater = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                boolean changed = false;
                while(!Thread.currentThread().isInterrupted())
                {
                    if(ITERATION_COUNT.get() > 550000 && !changed)
                    {
                        changed = true;
                        try
                        {
                            calculator = (Calculator) Class.forName("com.epickrram.talk.warmup.example.cha.SecondCalculator").newInstance();
                        }
                        catch (final Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        updater.setDaemon(true);
        updater.start();
        calculator = new FirstCalculator();
        System.out.println("DCE Guard: " + doWork());
    }

    private static int doWork()
    {
        int accumulator = 0;

        long loopStart = System.nanoTime();
        for(int i = 1; i < 1000000; i++)
        {
            accumulator += calculator.calculateResult(i);

            if(i % 1000 == 0 && i != 0)
            {
                final long loopDuration = System.nanoTime() - loopStart;

                System.out.println("Loop at " + i + " took " + loopDuration + " ns");
                LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(500L));

                loopStart = System.nanoTime();
                ITERATION_COUNT.lazySet(i);
            }
        }

        return accumulator;
    }
}