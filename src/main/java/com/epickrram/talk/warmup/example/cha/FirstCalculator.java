package com.epickrram.talk.warmup.example.cha;

public final class FirstCalculator implements Calculator
{
    @Override
    public int calculateResult(final int input)
    {
        long work = System.nanoTime();
        work /= 0.000000017d * input;
        work /= 0.000000037d / input;
        work *= 19;
        work = work * work;
        work /= 0.000000007d;
        work *= 42;

        return (int) work;
    }
}
