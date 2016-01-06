package com.epickrram.talk.warmup.example.cha;

/**
 * Example to demonstrate CHA by C2 compiler.
 *
 * Run with:
 *
 * -XX:+UnlockDiagnosticVMOptions -XX:+TraceClassLoading -XX:+TraceClassUnloading -XX:-TieredCompilation -XX:+LogCompilation -XX:CompileThreshold=20000
 */
public final class ClassHierarchyAnalysisExample
{
    public static void main(final String[] args)
    {
        System.out.println("DCE Guard: " + doWork(new FirstCalculator()));
    }

    private static int doWork(final Calculator calculator)
    {
        int accumulator = 0;

        long loopStart = System.nanoTime();
        for(int i = 1; i < 1000000; i++)
        {
            accumulator += maybeLoadOtherClass(i);
            accumulator += calculator.calculateResult(i);

            if(i % 1000 == 0 && i != 0)
            {
                final long loopDuration = System.nanoTime() - loopStart;

                System.out.println("Loop at " + i + " took " + loopDuration + " ns");

                loopStart = System.nanoTime();
            }
        }

        return accumulator;
    }

    private static int maybeLoadOtherClass(final int counter)
    {
        if(counter == 550001)
        {
            return new SecondCalculator().calculateResult(counter);
        }

        return 0;
    }
}