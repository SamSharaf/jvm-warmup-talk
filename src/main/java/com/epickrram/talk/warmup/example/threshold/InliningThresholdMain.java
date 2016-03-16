package com.epickrram.talk.warmup.example.threshold;

public class InliningThresholdMain
{
    public static void main(final String[] args) throws Exception
    {
        final int invocationCount = getInvocationCount(args);
        long accumulator = 17L;

        for(int i = 0; i < invocationCount - 1; i++)
        {
            accumulator += inlineCandidateCaller(accumulator);
        }
        for(int i = 0; i < 20; i++)
        {
            accumulator += inlineCandidateCaller(accumulator);
        }

        Thread.sleep(5000L);

        System.out.println("DCE guard: " + accumulator);
    }

    private static long inlineCandidateCaller(final long input)
    {
        return shouldInline(input);
    }

    private static long shouldInline(final long input)
    {
        return (input * System.nanoTime()) + 37L;
    }

    private static int getInvocationCount(final String[] args)
    {
        return args.length > 0 ? Integer.parseInt(args[0]) : 250;
    }
}