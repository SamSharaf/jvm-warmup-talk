package com.epickrram.talk.warmup.example.gotchas;

import java.util.concurrent.atomic.AtomicInteger;

public class DeOptExample
{
    private final AtomicInteger counter = new AtomicInteger();
    private volatile boolean flip = true;

    public static void main(String[] args)
    {
        DeOptExample deOptExample = new DeOptExample();
        deOptExample.init();
        deOptExample.run();
    }

    private void init()
    {
        new Thread(() -> {
            while(!Thread.currentThread().isInterrupted())
            {
                if(counter.get() > 85000)
                {
                    flip = false;
                    System.out.println("de-opt!");
                    return;
                }
            }
        }).start();
    }

    private void run()
    {
        long value = 17L;

        for(int i = 0; i < 1000000; i++)
        {
            value = incrementValue(value);
            counter.lazySet(i);
        }
    }

    private long incrementValue(long value)
    {
        if(flip)
        {
            value += firstMethod(value);
        }
        else
        {
            value +=  secondMethod(value);
        }
        return value;
    }

    private static long firstMethod(final long input)
    {
        return input * 7;
    }

    private static long secondMethod(final long input)
    {
        return input * 5;
    }
}