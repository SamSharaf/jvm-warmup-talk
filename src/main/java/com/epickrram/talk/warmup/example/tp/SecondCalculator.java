package com.epickrram.talk.warmup.example.tp;

public final class SecondCalculator implements Calculator
{
    @Override
    public int calculateResult(final int input)
    {
        long work = System.nanoTime();
        work /= 0.00000019d * input;
        work /= 0.00000017d / input;
        work *= 37;
        work = work * work;
        work /= 0.00000005d;
        work *= 42;

        return (int) work;
    }
}
