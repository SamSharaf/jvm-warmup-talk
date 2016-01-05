package com.epickrram.talk.warmup.example.hint;

public class CompiledMethodExample
{
    public static void main(final String[] args)
    {
        long loopStart = System.nanoTime();
        long total = 0L;
        for(int i = 0; i < 50000; i++)
        {
            total += performOps(i + 17);
            if(i % 1000 == 0 && i != 0)
            {
                final long loopDuration = System.nanoTime() - loopStart;

                System.out.println("Loop at " + i + " took " + loopDuration + " ns");

                loopStart = System.nanoTime();
            }
        }

        System.out.println("DCE guard: " + total);
    }

    private static int performOps(final int input)
    {
        int sum = 0;
        sum += input;
        sum *= input;
        sum -= input;
        sum /= input;
        sum *= 2;

        return sum;
    }
}