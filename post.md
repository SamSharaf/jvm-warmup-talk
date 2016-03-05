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


