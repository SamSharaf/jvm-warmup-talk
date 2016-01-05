package com.epickrram.talk.warmup.example.loop;

public final class FixedLoopCount
{
    public static int doLoop10()
    {
        int sum = 0;
        for(int i = 0; i < 10; i++)
        {
            sum += i;
        }

        return sum;
    }

    public static int doLoop100()
    {
        int sum = 0;
        for(int i = 0; i < 100; i++)
        {
            sum += i;
        }

        return sum;
    }

    public static int doLoop1000()
    {
        int sum = 0;
        for(int i = 0; i < 1000; i++)
        {
            sum += i;
        }

        return sum;
    }
}