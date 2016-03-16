Continuing on from my [last post](http://epickrram.blogspot.com/2016/03/observing-jvm-warm-up-effects.html), here we'll be looking at
flags used to control the C2 or server compiler of the Hotspot JVM.



## Configuration

In order to reduce the noise created in the compilation logs, we'll be disabling _tiered compilation_ so that only the server compiler
will be used. This is done using the following flag:

    -XX:-TieredCompilation


We'll also be producing more detailed compiler logs using:

    -XX:+UnlockDiagnosticVMOptions
    -XX:+LogCompilation

These flags will cause the JVM to generate a file called `hotspot_<pid>.log` in the current working directory, containing
detailed information on the operation of the compiler.


## C2 Compile Thresholds

The server compiler seems to behave in a slightly different manner to the profile-guided client compiler.

Looking at the flags related to _Tier4_ compilation options (Tier4 is the server compiler), we can see a similar set
to those for the Tier3 thresholds described in the last post:

    [mark@metal jvm-warmup-talk]$ java -XX:+PrintFlagsFinal 2>&1 | grep Tier4
         intx Tier4BackEdgeThreshold            = 40000
         intx Tier4CompileThreshold             = 15000
         intx Tier4InvocationThreshold          = 5000
         intx Tier4LoadFeedback                 = 3
         intx Tier4MinInvocationThreshold       = 600

So we might expect to be able to run the same experiments regarding the triggering of invocation, back-edge and compile thresholds.

In practice however when disabling tiered compilation, these thresholds do not seem to affect compilation in the same
way as the client compiler flags. In order to control the operation of the server compiler, we need to use the following flags:

    -XX:CompileThreshold
    -XX:BackEdgeThreshold

For the server compiler, `CompileThreshold` seems to act as the _invocation threshold_. Setting an artificially low
threshold (of `-XX:CompileThreshold=200`) shows this:

    [mark@metal jvm-warmup-talk]$ bash ./scripts/c2-invocation-threshold.sh
    LOG: Loop count is: 200
        181   75        com.epickrram.t.w.e.t.C2InvocationThresholdMain::
                                exerciseServerCompileThreshold (6 bytes)

Note that we are no longer seeing information about the _tier_ in the `PrintCompilation` output. In order to confirm
that the server compiler is operating here, we can look at the more detailed `LogCompilation` output for
compile task _75_:

    [mark@metal jvm-warmup-talk]$ grep "id='75'" hotspot_pid31473.log
    <task_queued compile_id='75'
        method='com/epickrram/talk/warmup/example/threshold/C2InvocationThresholdMain
                    exerciseServerCompileThreshold (J)J'
        bytes='6' count='100' backedge_count='1' iicount='200'
        stamp='0.089' comment='count' hot_count='200'/>

    <nmethod compile_id='75' compiler='C2' ...
        method='com/epickrram/talk/warmup/example/threshold/C2InvocationThresholdMain
                    exerciseServerCompileThreshold (J)J'
        bytes='6' count='100' backedge_count='1' iicount='200' stamp='0.182'/>

    <task compile_id='75'
        method='com/epickrram/talk/warmup/example/threshold/C2InvocationThresholdMain
                    exerciseServerCompileThreshold (J)J'
        bytes='6' count='100' backedge_count='1' iicount='200' stamp='0.182'>

We can see that the compiler being used in this compile task is *C2* and that the interpreter invocation count _iicount_ is *200*.


## C2 BackEdge Threshold


The server compiler's handling of loop back-edge thresholds seems to differ again from the tiered C1 flags. Using this
[example program](https://github.com/epickrram/jvm-warmup-talk/blob/master/src/main/java/com/epickrram/talk/warmup/example/threshold/C2LoopBackedgeThresholdMain.java)
we can see that an on-stack replacement is triggered when the back-edge count is *14563*.

This is despite the `BackEdgeThreshold` flag value being set to a lower value. No amount of threshold-wrangling makes the JVM
exhibit the same behaviour as the client compiler in terms of the relationship between the _Tier3_ `InvocationThreshold`, `CompileThreshold` and
`BackEdgeThreshold`.


    [mark@metal jvm-warmup-talk]$ bash ./scripts/c2-loop-backedge-threshold.sh 14600
    LOG: Loop count is: 14600
        133    2 %  com.epickrram.t.w.e.t.C2LoopBackedgeThresholdMain::
                            exerciseServerLoopBackedgeThreshold @ 5 (25 bytes)

    [mark@metal jvm-warmup-talk]$ grep "id='2'" hotspot_pid32675.log
    <task_queued compile_id='2' compile_kind='osr'
        method='com/epickrram/talk/warmup/example/threshold/C2LoopBackedgeThresholdMain
                    exerciseServerLoopBackedgeThreshold (JI)J'
        bytes='25' count='1' backedge_count='14563' iicount='1' osr_bci='5'
        stamp='0.134' comment='backedge_count' hot_count='14563'/>

    <nmethod compile_id='2' compile_kind='osr' compiler='C2' ...
        method='com/epickrram/talk/warmup/example/threshold/C2LoopBackedgeThresholdMain
                    exerciseServerLoopBackedgeThreshold (JI)J' bytes='25'
        count='10000' backedge_count='5037' iicount='1' stamp='0.136'/>

    <task compile_id='2' compile_kind='osr'
        method='com/epickrram/talk/warmup/example/threshold/C2LoopBackedgeThresholdMain
                    exerciseServerLoopBackedgeThreshold (JI)J'
        bytes='25' count='10000' backedge_count='5037' iicount='1' osr_bci='5' stamp='0.134'>


What is interesting is that the _nmethod_ node contains a *count* that is equal to the value of `-XX:CompileThreshold`. If we reduce this
threshold to *5000*, we can see that the on-stack replacement happens sooner:


    [mark@metal jvm-warmup-talk]$ bash ./scripts/c2-loop-backedge-threshold.sh
    LOG: Loop count is: 10000
        126    6 %    com.epickrram.talk.warmup.example.threshold.C2LoopBackedgeThresholdMain::
                            exerciseServerLoopBackedgeThreshold @ 5 (25 bytes)

    [mark@metal jvm-warmup-talk]$ grep "id='6'" hotspot_pid1598.log
    <task_queued compile_id='6' compile_kind='osr'
        method='com/epickrram/talk/warmup/example/threshold/C2LoopBackedgeThresholdMain
                    exerciseServerLoopBackedgeThreshold (JI)J'
        bytes='25' count='1' backedge_count='7793' iicount='1' osr_bci='5'
        stamp='0.126' comment='backedge_count' hot_count='7793'/>

    <nmethod compile_id='6' compile_kind='osr' compiler='C2' ...
        method='com/epickrram/talk/warmup/example/threshold/C2LoopBackedgeThresholdMain
                    exerciseServerLoopBackedgeThreshold (JI)J' bytes='25'
        count='5000' backedge_count='2659' iicount='1' stamp='0.128'/>

    <task compile_id='6' compile_kind='osr'
        method='com/epickrram/talk/warmup/example/threshold/C2LoopBackedgeThresholdMain
                    exerciseServerLoopBackedgeThreshold (JI)J'
        bytes='25' count='5000' backedge_count='2659' iicount='1' osr_bci='5' stamp='0.126'>

Here, OSR occurs after a back-edge count of *7793*, while the _nmethod_ node has *count='5000'*.

From these observations, we can infer that loop back-edge compilation triggers are related to the `CompileThreshold` flag, and that
if we wish to control when the server compiler kicks in, we need to alter only the `CompileThreshold` flag.




## Inlining


When a method is converted to a native method, the compiler has the option to perform a further optimisation: _inlining_.

Inlining callee methods reduce method-dispatch overhead, and can allow the compiler a broader scope for further optimisation,
e.g. dead-code elimination or escape analysis.


Inlining decisions are based on the size of the method to be inlined. There are two thresholds that we need be concerned with:


    -XX:MaxInlineSize
    -XX:FreqInlineSize


These thresholds are specified in byte-codes. Let's start with an example of a method that is small enough for inlining:

    private static long shouldInline(final long input)
    {
        return (input * System.nanoTime()) + 37L;
    }

Using `javap` to inspect the byte-code of this method, we can see that it is only 9 byte-codes in length:

    private static long shouldInline(long);
        descriptor: (J)J
        flags: ACC_PRIVATE, ACC_STATIC
        Code:
        stack=4, locals=2, args_size=1
            0: lload_0
            1: invokestatic  #18   // Method java/lang/System.nanoTime:()J
            4: lmul
            5: ldc2_w        #19   // long 37l
            8: ladd
            9: lreturn

Running the example and adding the `-XX:+PrintInlining` flag will cause the compiler to interleave information about
inlining decisions into the compilation output.


    [mark@metal jvm-warmup-talk]$ bash ./scripts/small-method-inlining-threshold.sh 250
    72  19  3       com.epickrram.talk.warmup.example.threshold.InliningThresholdMain::
                            inlineCandidateCaller (5 bytes)
                        @ 1   com.epickrram.talk.warmup.example.threshold.InliningThresholdMain::
                                    shouldInline (10 bytes)
                          @ 1   java.lang.System::nanoTime (0 bytes)   intrinsic
    72  20  3       com.epickrram.talk.warmup.example.threshold.InliningThresholdMain::
                            shouldInline (10 bytes)
                          @ 1   java.lang.System::nanoTime (0 bytes)   intrinsic

In this log excerpt, we can see that after the usual output of `PrintCompilation`, we also get information about methods being
inlined.

If we look at the byte-code of the caller method:

    private static long inlineCandidateCaller(long);
        descriptor: (J)J
        flags: ACC_PRIVATE, ACC_STATIC
        Code:
        stack=2, locals=2, args_size=1
            0: lload_0
            1: invokestatic  #17      // Method shouldInline:(J)J
            4: lreturn


Here we can see that the invocation of the _shouldInline_ method is at byte-code *1*, so the output of `PrintInlining` is
referring to the call-site that is inlined (the *@ 1* part of the log entry).

If we reduce the `MaxInlineSize` parameter to be less than 10 byte-codes using `-XX:MaxInlineSize=9`, then inlining will fail:

    [mark@metal jvm-warmup-talk]$ bash ./scripts/reduced-inlining-threshold.sh 250
    105  19  3       com.epickrram.talk.warmup.example.threshold.InliningThresholdMain::
                                    inlineCandidateCaller (5 bytes)
                       @ 1   com.epickrram.talk.warmup.example.threshold.InliningThresholdMain::
                                        shouldInline (10 bytes)   callee is too large


Note the message *callee is too large* - this is something to look out for if you expect methods in hot code-paths to be inlined;
it means that the compiler did not inline this method due to its size.


Now, the default value of `MaxInlineSize` is 20 byte-codes, which is not a lot of code. The compilation process is a trade-off
between achieving good performance, and the space overhead of compiled code, among other things.

The compiler _will_ inline your 21 byte-code method, _if it is called often enough_. In called frequently enough, the size
threshold that determines inlining is `FreqInlineSize`.

Let's re-run our experiment, and increase the number of invocations:

    [mark@metal jvm-warmup-talk]$ bash ./scripts/reduced-inlining-threshold.sh 25000
    79  19  3       com.epickrram.talk.warmup.example.threshold.InliningThresholdMain::
                            inlineCandidateCaller (5 bytes)
                       @ 1   com.epickrram.talk.warmup.example.threshold.InliningThresholdMain::
                                    shouldInline (10 bytes)   callee is too large

    ...

    80  22  4       com.epickrram.talk.warmup.example.threshold.InliningThresholdMain::
                            shouldInline (10 bytes)
                       @ 1   java.lang.System::nanoTime (0 bytes)   (intrinsic)
                       @ 1   com.epickrram.talk.warmup.example.threshold.InliningThresholdMain::
                                    shouldInline (10 bytes)   inline (hot)


First, we see the same message declaring the callee method to be too large, but later on in the compilation process, the callee method is
inlined. This corresponds with the message *inline (hot)*, meaning that the runtime has decided this method is called frequently
enough to inline.

If we reduce the `FreqInlineSize` to be less than 10 byte-codes using `-XX:FreqInlineSize=9`, then inline will once again fail:

    [mark@metal jvm-warmup-talk]$ bash ./scripts/reduced-freq-inlining-threshold.sh 25000
    77  22  4       com.epickrram.talk.warmup.example.threshold.InliningThresholdMain::
                            shouldInline (10 bytes)
                      @ 1   java.lang.System::nanoTime (0 bytes)   (intrinsic)
                      @ 1   com.epickrram.talk.warmup.example.threshold.InliningThresholdMain::
                                    shouldInline (10 bytes)   hot method too big


Here denoted by the message *hot method too big*.


## Summary

We have seen that further to the Tier3 client compiler thresholds, the compilation of longer-running programs will controlled by server-specific thresholds.

Inlining decisions are based on the size of the callee method, and the frequency with which is it called. The Hotspot compiler will attempt to aggressively
inline hot methods, so it is important to understand whether the design of our code is hindering the ability of the compiler to perform available optimisations.

In my next post, I'll be looking at some of the tooling available to help analyse and understand the operation of the JVM Hotspot compiler.
