In this post, we will explore some of the various flags that can affect the operation of the JVM's JIT compiler.

Anything demonstrated in this post should come with a public health warning - these options are explored for reference only,
and modifying them without being able to observe and reason about their effects should be avoided.

My view on working with the JIT compiler is this: the JIT compiler, and the engineers who work on it are much better at
performance tuning that I could ever hope to be.

You have been warned.


## Thresholds

At a very high level, the JVM bytecode interpreter uses method invocation and loop back-edge counting in order to decide when a method should be compiled.

Since it would be wasteful and expensive to compile methods that are only ever called a small number of times,
the interpreter will wait until a method invocation count is over a particular threshold before it is compiled.

Thresholds for various levels of compilation can be modified using flags passed to the JVM on the command line.

The first such threshold that is likely to be triggered is the C1 Compilation Threshold.

## Flags side-note

To view all the available flags that can be passed to the jvm, run the following command:

`
java -XX:+PrintFlagsFinal
`

Running this on my local install of `JDK 1.8.0_60-b27` shows that there are 772 flags available:

`
[pricem@metal ~]$ java -XX:+PrintFlagsFinal 2>&1 | wc -l
772
`

For the truly intrepid, there are even more tunables available if we unlock diagnostic options (more on this later):

`
[pricem@metal ~]$ java -XX:+UnlockDiagnosticVMOptions -XX:+PrintFlagsFinal 2>&1 | wc -l
873
`

## Example code

Code samples from this post are available on [github](https://github.com/epickrram/jvm-warmup-talk).

Clone the repository, then build with:

`
./gradlew build
`



## C1 Compilation Threshold

The first trigger that a method is likely to hit is for C1 compilation threshold.
This threshold is specified by the flag:


    [pricem@metal ~]$ java -XX:+PrintFlagsFinal 2>&1 | grep Tier3InvocationThreshold
    intx Tier3InvocationThreshold                  = 200                                 {product}

This setting informs the interpreter that it should emit a compile task to the C1 compiler when an interpreted method is
executed `200` times.

Observing this should be simple - all we need to do is
[write a method](https://github.com/epickrram/jvm-warmup-talk/blob/master/src/main/java/com/epickrram/talk/warmup/example/threshold/C1CompilationThresholdMain.java#L43),
call it `200` times and observe the compiler doing its work.

Observing the compiler operation is a simple matter of supplying another JVM argument on start-up:

`
-XX:+PrintCompilation
`

Without further ado, let us try to observe our method being compiled after `200` invocations.
The script being called will log any statements from the program, and also any other output to stdout that is relevant to compilations for this project.

We would expect to see a message saying that the
[exerciseTier3CompileThreshold](https://github.com/epickrram/jvm-warmup-talk/blob/master/src/main/java/com/epickrram/talk/warmup/example/threshold/C1CompilationThresholdMain.java#L43)
method is compiled.


    [pricem@metal jvm-warmup-talk]$ bash ./scripts/c1-invocation-threshold.sh
    LOG: Loop count is: 200


No compilation message. I'll shortcut a bit of investigation here and point out that the Tier3 compile threshold seems to work on boundaries of power-two numbers:


    [pricem@metal jvm-warmup-talk]$ bash ./scripts/c1-invocation-threshold.sh 255
    LOG: Loop count is: 255


    [pricem@metal jvm-warmup-talk]$ bash ./scripts/c1-invocation-threshold.sh 256
    LOG: Loop count is: 256
        132   47       3       com.epickrram.talk.warmup.example.threshold.C1CompilationThresholdMain::exerciseTier3CompileThreshold (6 bytes)


This pattern is repeated for larger numbers:


    [pricem@metal jvm-warmup-talk]$ java -cp build/libs/jvm-warmup-talk-0.0.1.jar -XX:+PrintCompilation -XX:Tier3InvocationThreshold=1000 com.epickrram.talk.warmup.example.threshold.C1CompilationThresholdMain 1023 | grep -E "(LOG|epickrram)"
    LOG: Loop count is: 1023


    [pricem@metal jvm-warmup-talk]$ java -cp build/libs/jvm-warmup-talk-0.0.1.jar -XX:+PrintCompilation -XX:Tier3InvocationThreshold=1000 com.epickrram.talk.warmup.example.threshold.C1CompilationThresholdMain 1024 | grep -E "(LOG|epickrram)"
    LOG: Loop count is: 1024
        128   18       3       com.epickrram.talk.warmup.example.threshold.C1CompilationThresholdMain::exerciseTier3CompileThreshold (6 bytes)


## C1 Loop Back-edge Threshold

As mentioned earlier, the JVM bytecode interpreter will also monitor loop counts within a method.
This mechanism allows the runtime to spot that a method is *hot* despite it not being invoked many times.

For example, if we have a method that contains a loop executing many thousands of times, we would want that method to be compiled,
even if it was only invoked relatively infrequently.

The relevant flag for this setting is:

`Tier3BackEdgeThreshold`


    [pricem@metal jvm-warmup-talk]$ java -XX:+PrintFlagsFinal 2>&1 | grep Tier3BackEdgeThreshold
         intx Tier3BackEdgeThreshold                    = 60000                               {product}


Using [another example program](https://github.com/epickrram/jvm-warmup-talk/blob/master/src/main/java/com/epickrram/talk/warmup/example/threshold/C1LoopBackedgeThresholdMain.java),
we can observe the interpreter emitting a compile task once the loop count within a method reaches the specified threshold:


    [pricem@metal jvm-warmup-talk]$ bash scripts/c1-loop-backedge-threshold.sh 60416
    LOG: Loop count is: 60416
        137   48 %     3       com.epickrram.talk.warmup.example.threshold.C1LoopBackedgeThresholdMain::exerciseTier3LoopBackedgeThreshold @ 5 (25 bytes)


Once again, there seems to be a slight difference in the required number of loop iterations and the specified threshold.
In this case, we need to execute the loop `60416` times in order for the interpreter to recognise this method as *hot*.
`60416` just happens to be `1024 * 59`, it's almost as though there's a pattern here...


## PrintCompilation format

In order to understand what is happening here, we need to take a brief foray into understanding the output from the `PrintCompilation` command.

Rather than draw my own fancy graphic, I'm going to reference a slide from Doug Hawkins' excellent talk [_JVM Mechanics_](http://www.slideshare.net/dougqh/jvm-mechanics-when-does-the).


![PrintCompilation log format](https://raw.githubusercontent.com/epickrram/jvm-warmup-talk/master/img/print-compilation-format.png "PrintCompilation format")


Using this reference, we can break down the information in the log output from our test program:

        137   48 %     3       com.epickrram.talk.warmup.example.threshold.C1LoopBackedgeThresholdMain::exerciseTier3LoopBackedgeThreshold @ 5 (25 bytes)

1. This compile happened 137 milliseconds after JVM startup
2. Compilation ID was 48
3. This was an _on-stack replacement_ (more on this later)
4. This compilation happened at Tier3 (C1 profile-guided)
5. The OSR loop bytecode index is _5_
6. The compiled method was 25 bytecodes


## Verifying the detail


Let's go and take a quick look at what these bytecode references are. If we decompile the method using `javap`:

    [pricem@metal jvm-warmup-talk]$ javap -cp build/libs/jvm-warmup-talk-0.0.1.jar -c -p com.epickrram.talk.warmup.example.threshold.C1LoopBackedgeThresholdMain

We can see the disassembled bytecode of the method in question:

      private static long exerciseTier3LoopBackedgeThreshold(long, int);
        Code:
           0: lload_0
           1: lstore_3
           2: iconst_0
           3: istore        5
           5: iload         5
           7: iload_2
           8: if_icmpge     23
          11: ldc2_w        #22                 // long 17l
          14: lload_3
          15: lmul
          16: lstore_3
          17: iinc          5, 1
          20: goto          5
          23: lload_3
          24: lreturn

This tells us that the method contains 25 bytecodes, so that explains one number. We can also see the `goto` instruction at bytecode index `20`, and its target bytecode index `5`.

Comparing this with the method source:

    private static long exerciseTier3LoopBackedgeThreshold(final long input, final int loopCount)
    {
        long value = input;
        for(int i = 0; i < loopCount; i++)
        {
            value = 17L * value;
        }

        return value;
    }

With a little bit of reasoning, we can figure out that bytecode `5` is the point at which we load the loop counter variable _i_ in order to do the comparison to the _loopCount_ parameter.

This bytecode index then, is at the start of the loop, and would be an ideal place to jump to executing the newly compiled method.


## On-Stack Replacement

On-Stack replacement is a mechanism that allows the interpreter to take advantage of compiled code, even when it is still executing a loop for that method in interpreted mode.

If we imagine a hypothetical workflow for our JVM to be:

1. Start executing a method _loopyMethod_ in the interpreter
2. Within _loopyMethod_, we execute an expensive loop body 1,000,000 times
3. The interpreter will see that the loop count has exceeded the Tier3BackedgeThreshold setting
4. The interpreter will request compilation of _loopyMethod_
5. The method body is expensive and slow, and we want to start using the compiled version immediately.
Without OSR, the interpreter would have to complete the 1,000,000 iterations of slow interpreted code,
 dispatching to the complied method on the next call to _loopyMethod()_
6. With OSR, the interpreter can dispatch to the compiled frame at the start of the next loop iteration
7. Execution will now continue in the compiled method body


