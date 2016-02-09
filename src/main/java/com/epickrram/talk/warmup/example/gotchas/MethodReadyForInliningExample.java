package com.epickrram.talk.warmup.example.gotchas;

public final class MethodReadyForInliningExample
{
    public Accumulator accumulator = new Accumulator();

    public static void main(String[] args)
    {
        long value = 0L;
        final MethodReadyForInliningExample example = new MethodReadyForInliningExample();
        do
        {
            value = example.longMethod(value);
        } while(value != Long.MIN_VALUE);
    }

    private long longMethod(final long input)
    {
        long value = bitShifting(input);
        value = pow(value);

        value = multiply(value);

        value = sqrt(value);

        value = addition(value);
        value = square(value);

        checkMinValue(value);

        checkMaxValue(value);

        accumulator.increment(value);

        return value;
    }

    private void checkMaxValue(long value)
    {
        if(value == Long.MAX_VALUE)
        {
            System.out.println("Found an unexpected value");

            throw new RuntimeException("Boom!");
        }
    }

    private void checkMinValue(long value)
    {
        if(value == Long.MIN_VALUE)
        {
            System.out.println("Found an unexpected value");

            throw new RuntimeException("Boom!");
        }
    }

    private long square(long value)
    {
        return value * value;
    }

    private long addition(long value)
    {
        return value + 37L;
    }

    private long multiply(long value)
    {
        return value * 17L;
    }

    private long sqrt(long value)
    {
        return (long) Math.sqrt(value);
    }

    private long pow(long value)
    {
        long pow = value;
        for(int i = 0; i < 2; i++)
        {
            pow = square(pow);
        }
        return pow;
    }

    private static long bitShifting(long input)
    {
        long value = input << 2;
        value = value | 0xDEADBEEFL;
        value = value >>> 2;
        return value;
    }

    private static class Accumulator
    {
        public long totalValue;

        void increment(final long value)
        {
            totalValue += value;
        }
    }
}
