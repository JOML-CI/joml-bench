package bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.ByteOrder.nativeOrder;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.openjdk.jmh.annotations.Mode.AverageTime;
import static org.openjdk.jmh.annotations.Scope.Benchmark;

@State(Benchmark)
@OutputTimeUnit(NANOSECONDS)
@Warmup(iterations = 10, time = 1000, timeUnit = MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = MILLISECONDS)
@BenchmarkMode(AverageTime)
@Fork(value = 1, jvmArgsAppend = {
        "-Djava.library.path=./native/build",
        "-XX:UseAVX=3",
        "--enable-preview",
        "--add-modules", "jdk.incubator.vector",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+EnableJVMCI",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.code.site=ALL-UNNAMED",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.meta=ALL-UNNAMED",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.runtime=ALL-UNNAMED",
        "-Djdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK=0"})
public class Bench {
    private final Matrix4f m4a = new Matrix4f();
    private final Matrix4f m4b = new Matrix4f();
    private final Matrix4f m4c = new Matrix4f();
    private final Matrix4fn m4na = new Matrix4fn();
    private final Matrix4fn m4nb = new Matrix4fn();
    private final Matrix4fvBB m4vbb = new Matrix4fvBB();
    private final Matrix4fvArr m4varr = new Matrix4fvArr();
    private final ByteBuffer bb = allocateDirect(16<<2).order(nativeOrder());
    private final long bb_addr = WithJvmci.address(bb);
    private final FloatBuffer fb = bb.asFloatBuffer();

    @Benchmark
    public void mul_Matrix4f_Jvmci_AVX() {
        WithJvmci.mulAvx(m4a, m4b, m4c);
    }

    @Benchmark
    public void mul_Matrix4f_Jvmci_AVX2() {
        WithJvmci.mulAvx2(m4a, m4b, m4c);
    }

    @Benchmark
    public void invert_Matrix4f_Jvmci() {
        WithJvmci.invert(m4a, m4b);
    }

    @Benchmark
    public void transpose_Matrix4f_Jvmci() {
        WithJvmci.transpose(m4a, m4b);
    }

    @Benchmark
    public void noop_jni() {
        m4na.noop(m4nb);
    }

    @Benchmark
    public void noop_Jvmci_2args() {
        WithJvmci.noop_2_args(m4a, 0L);
    }

    @Benchmark
    public void mul_Matrix4fn_SSE() {
        m4na.mulSSE(m4nb);
    }

    @Benchmark
    public void mul_Matrix4fn_AVX() {
        m4na.mulAVX(m4nb);
    }

    @Benchmark
    public void transpose_Matrix4f() {
        m4a.transpose(m4b);
    }

    @Benchmark
    public void transpose_Matrix4fvArr_128() {
        m4varr.transpose(m4varr);
    }

    @Benchmark
    public void invert_Matrix4f() {
        m4a.invert(m4b);
    }

    @Benchmark
    public void invert_Matrix4fvArr_128() {
        m4varr.invert128(m4varr);
    }

    @Benchmark
    public void store_Matrix4f_FloatBuffer_put() {
        m4a.storePutFB(fb);
    }

    @Benchmark
    public void store_Matrix4f_Jvmci_AVX2() {
        WithJvmci.storeAvx2(m4a, bb_addr);
    }

    @Benchmark
    public void store_Matrix4f_ByteBuffer_put() {
        m4a.storePutBB(bb);
    }

    @Benchmark
    public void store_Matrix4fvArr_FloatBuffer_put() {
        m4varr.storePut(fb);
    }

    @Benchmark
    public void store_Matrix4fvArr_Unsafe() {
        m4varr.storeU(bb);
    }

    @Benchmark
    public void store_Matrix4fvArr_256() {
        m4varr.storeV256(bb);
    }

    @Benchmark
    public void store_Matrix4fvArr_512() {
        m4varr.storeV512(bb);
    }

    @Benchmark
    public void store_Matrix4f_Unsafe() {
        m4a.storeU(bb);
    }

    @Benchmark
    public Object mul_Matrix4f() {
        return m4a.mul(m4b);
    }

    @Benchmark
    public Object mul_Matrix4f_FMA() {
        return m4a.mulFma(m4b);
    }

    @Benchmark
    public Object mulAffine_Matrix4f_FMA() {
        return m4a.mulAffineFma(m4b);
    }

    @Benchmark
    public Object mul_Matrix4fvArr_256() {
        return m4varr.mul256(m4varr);
    }

    @Benchmark
    public Object mul_Matrix4fvArr_128_Unrolled() {
        return m4varr.mul128Unrolled(m4varr);
    }

    @Benchmark
    public Object mul_Matrix4fvArr_128_Loop() {
        return m4varr.mul128Loop(m4varr);
    }

    @Benchmark
    public Object mul_Matrix4fvBB_256() {
        return m4vbb.mul256(m4vbb);
    }

    @Benchmark
    public Object mul_Matrix4fvBB_128_Unrolled() {
        return m4vbb.mul128Unrolled(m4vbb);
    }

    @Benchmark
    public Object mul_Matrix4fvBB_128_Loop() {
        return m4vbb.mul128Loop(m4vbb);
    }

    public static void main(String[] args) throws Exception {
        new Runner(new OptionsBuilder()
            .include(Bench.class.getName())
            .build()
        ).run();
    }
}
