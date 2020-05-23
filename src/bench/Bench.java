package bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.openjdk.jmh.annotations.Mode.AverageTime;
import static org.openjdk.jmh.annotations.Scope.Benchmark;

@State(Benchmark)
@OutputTimeUnit(NANOSECONDS)
@Warmup(iterations = 5, time = 1000, timeUnit = MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = MILLISECONDS)
@BenchmarkMode(AverageTime)
@Fork(1)
public class Bench {
    private final Matrix4f m4 = new Matrix4f();
    private final Matrix4fvBB m4vbb = new Matrix4fvBB();
    private final Matrix4fvArr m4varr = new Matrix4fvArr();
    private final Matrix4fn m4n = new Matrix4fn();

    @Benchmark
    public Object noopJni() {
        return m4n.noop(m4n);
    }

    @Benchmark
    public Object mulJniSSE() {
        return m4n.mulSSE(m4n);
    }

    @Benchmark
    public Object mulJniAVX() {
        return m4n.mulAVX(m4n);
    }

    @Benchmark
    public Object mulScalar() {
        return m4.mul(m4);
    }

    @Benchmark
    public Object mulScalarFma() {
        return m4.mulFma(m4);
    }

    @Benchmark
    public Object mul256Arr() {
        return m4varr.mul256(m4varr);
    }

    @Benchmark
    public Object mul128UnrolledArr() {
        return m4varr.mul128Unrolled(m4varr);
    }

    @Benchmark
    public Object mul128LoopArr() {
        return m4varr.mul128Loop(m4varr);
    }

    @Benchmark
    public Object mul256BB() {
        return m4vbb.mul256(m4vbb);
    }

    @Benchmark
    public Object mul128UnrolledBB() {
        return m4vbb.mul128Unrolled(m4vbb);
    }

    @Benchmark
    public Object mul128LoopBB() {
        return m4vbb.mul128Loop(m4vbb);
    }

    public static void main(String[] args) throws Exception {
        new Runner(new OptionsBuilder()
            .include(Bench.class.getName())
            //.addProfiler(LinuxPerfProfiler.class)
            .jvmArgsAppend(
                    "--add-modules=jdk.incubator.vector",
                    "-Djdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK=0"
                  //"-XX:+UnlockExperimentalVMOptions",
                  //"-XX:+UseJVMCICompiler" // <- Graal does not have Panama Vector API support right now
            )
            .build()
        ).run();
    }
}
