package bench;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorShuffle;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;

import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.ByteOrder.nativeOrder;
import static jdk.incubator.vector.FloatVector.*;

/**
 * 4x4 matrix backed by direct ByteBuffer.
 */
public class Matrix4fv {
    /*
     * Having the VectorShuffles as static final fields here is MUUUCH faster than creating them inline in the methods!!!
     */
    private static final VectorShuffle<Float> s0000 = SPECIES_128.shuffleFromValues(0, 0, 0, 0);
    private static final VectorShuffle<Float> s1111 = SPECIES_128.shuffleFromValues(1, 1, 1, 1);
    private static final VectorShuffle<Float> s2222 = SPECIES_128.shuffleFromValues(2, 2, 2, 2);
    private static final VectorShuffle<Float> s3333 = SPECIES_128.shuffleFromValues(3, 3, 3, 3);
    private static final VectorShuffle<Float> s00004444 = SPECIES_256.shuffleFromValues(0, 0, 0, 0, 4, 4, 4, 4); //_MM_SHUFFLE(0, 0, 0, 0)
    private static final VectorShuffle<Float> s11115555 = SPECIES_256.shuffleFromValues(1, 1, 1, 1, 5, 5, 5, 5); //_MM_SHUFFLE(1, 1, 1, 1)
    private static final VectorShuffle<Float> s22226666 = SPECIES_256.shuffleFromValues(2, 2, 2, 2, 6, 6, 6, 6); //_MM_SHUFFLE(2, 2, 2, 2)
    private static final VectorShuffle<Float> s33337777 = SPECIES_256.shuffleFromValues(3, 3, 3, 3, 7, 7, 7, 7); //_MM_SHUFFLE(3, 3, 3, 3)
    private static final VectorShuffle<Float> s01230123 = SPECIES_256.shuffleFromValues(0, 1, 2, 3, 0, 1, 2, 3); //_mm256_permute2f128_ps(..., 0x00);
    private static final VectorShuffle<Float> s45674567 = SPECIES_256.shuffleFromValues(4, 5, 6, 7, 4, 5, 6, 7); //_mm256_permute2f128_ps(..., 0x11);

    private final ByteBuffer es = allocateDirect(16 << 2).order(nativeOrder());

    public Matrix4fv() {
        // Initialize to identity
        es.putFloat(0, 1f);
        es.putFloat(5<<2, 1f);
        es.putFloat(10<<2, 1f);
        es.putFloat(15<<2, 1f);
    }

    public Matrix4fv mul128Loop(Matrix4fv o) {
        /*
         * Adapted from:
         * https://stackoverflow.com/questions/18499971/efficient-4x4-matrix-multiplication-c-vs-assembly#answer-18508113
         */
        FloatVector row1 = fromByteBuffer(SPECIES_128, o.es, 0, nativeOrder());
        FloatVector row2 = fromByteBuffer(SPECIES_128, o.es, 16, nativeOrder());
        FloatVector row3 = fromByteBuffer(SPECIES_128, o.es, 32, nativeOrder());
        FloatVector row4 = fromByteBuffer(SPECIES_128, o.es, 48, nativeOrder());
        for (int i = 0; i < 4; i++) {
            FloatVector r = fromByteBuffer(SPECIES_128, es, i<<4, nativeOrder());
            // _mm_set1_ps(A[4*i + 0])
            FloatVector b0 = r.rearrange(s0000);
            // _mm_set1_ps(A[4*i + 1])
            FloatVector b1 = r.rearrange(s1111);
            // _mm_set1_ps(A[4*i + 2])
            FloatVector b2 = r.rearrange(s2222);
            // _mm_set1_ps(A[4*i + 3])
            FloatVector b3 = r.rearrange(s3333);
            b0.fma(row1, b1.fma(row2, b2.fma(row3, b3.mul(row4)))).intoByteBuffer(es, i<<4, nativeOrder());
        }
        return this;
    }

    public Matrix4fv mul128Unrolled(Matrix4fv o) {
        /*
         * Adapted from:
         * https://stackoverflow.com/questions/18499971/efficient-4x4-matrix-multiplication-c-vs-assembly#answer-18508113
         */
        FloatVector row1 = fromByteBuffer(SPECIES_128, o.es, 0, nativeOrder());
        FloatVector row2 = fromByteBuffer(SPECIES_128, o.es, 16, nativeOrder());
        FloatVector row3 = fromByteBuffer(SPECIES_128, o.es, 32, nativeOrder());
        FloatVector row4 = fromByteBuffer(SPECIES_128, o.es, 48, nativeOrder());
        FloatVector r0, r1, r2, r3;
        r0 = fromByteBuffer(SPECIES_128, es, 0, nativeOrder());
        r0.rearrange(s0000).fma(row1, r0.rearrange(s1111).fma(row2, r0.rearrange(s2222).fma(row3, r0.rearrange(s3333).mul(row4)))).intoByteBuffer(es, 0, nativeOrder());
        r1 = fromByteBuffer(SPECIES_128, es, 16, nativeOrder());
        r1.rearrange(s0000).fma(row1, r1.rearrange(s1111).fma(row2, r1.rearrange(s2222).fma(row3, r1.rearrange(s3333).mul(row4)))).intoByteBuffer(es, 16, nativeOrder());
        r2 = fromByteBuffer(SPECIES_128, es, 32, nativeOrder());
        r2.rearrange(s0000).fma(row1, r2.rearrange(s1111).fma(row2, r2.rearrange(s2222).fma(row3, r2.rearrange(s3333).mul(row4)))).intoByteBuffer(es, 32, nativeOrder());
        r3 = fromByteBuffer(SPECIES_128, es, 48, nativeOrder());
        r3.rearrange(s0000).fma(row1, r3.rearrange(s1111).fma(row2, r3.rearrange(s2222).fma(row3, r3.rearrange(s3333).mul(row4)))).intoByteBuffer(es, 48, nativeOrder());
        return this;
    }

    public Matrix4fv mul256(Matrix4fv o) {
        /*
         * Adapted from:
         * https://stackoverflow.com/questions/19806222/matrix-vector-multiplication-in-avx-not-proportionately-faster-than-in-sse#answer-46058667
         */
        FloatVector t0 = fromByteBuffer(SPECIES_256, es, 0, nativeOrder());
        FloatVector t1 = fromByteBuffer(SPECIES_256, es, 32, nativeOrder());
        FloatVector u0 = fromByteBuffer(SPECIES_256, o.es, 0, nativeOrder());
        FloatVector u1 = fromByteBuffer(SPECIES_256, o.es, 32, nativeOrder());
        FloatVector u0r00 = u0.rearrange(s01230123);
        FloatVector u1r00 = u1.rearrange(s01230123);
        FloatVector u0r11 = u0.rearrange(s45674567);
        FloatVector u1r11 = u1.rearrange(s45674567);
        t0.rearrange(s00004444).fma(u0r00, t0.rearrange(s11115555).mul(u0r11))
                .add(t0.rearrange(s33337777).fma(u1r11, t0.rearrange(s22226666).mul(u1r00)))
                .intoByteBuffer(es, 0, nativeOrder());
        t1.rearrange(s00004444).fma(u0r00, t1.rearrange(s11115555).mul(u0r11))
                .add(t1.rearrange(s33337777).fma(u1r11, t1.rearrange(s22226666).mul(u1r00)))
                .intoByteBuffer(es, 32, nativeOrder());
        return this;
    }

    @Override
    public String toString() {
        DecimalFormat f = new DecimalFormat(" 0.000E0;-");
        FloatBuffer fb = es.asFloatBuffer();
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                str.append(f.format(fb.get(j * 4 + i)));
            }
            if (i < 3) {
                str.append("\n");
            }
        }
        return str.toString();
    }

    public static void main(String[] args) {
        System.out.println(new Matrix4fv().mul128Loop(new Matrix4fv()));
        System.out.println("----");
        System.out.println(new Matrix4fv().mul128Unrolled(new Matrix4fv()));
        System.out.println("----");
        System.out.println(new Matrix4fv().mul256(new Matrix4fv()));
    }

}
