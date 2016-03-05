package com.epickrram.talk.warmup.example.log;

public final class StdoutLogger
{
    public static void log(final String formatString, final Object... args)
    {
        System.out.printf("LOG: %s", String.format(formatString, args));
    }
}
