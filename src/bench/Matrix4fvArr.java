package bench;

import jdk.incubator.vector.FloatVector;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;

import static bench.Matrix4fv.*;
import static java.nio.ByteOrder.nativeOrder;
import static jdk.incubator.vector.FloatVector.*;

/**
 * 4x4 matrix backed by direct ByteBuffer.
 */
public class Matrix4fvArr {

    private final float[] es = new float[16];

    public Matrix4fvArr() {
        // Initialize to identity
        es[0] = 1f;
        es[5] = 1f;
        es[10] = 1f;
        es[15] = 1f;
    }

    public Matrix4fvArr mul128Loop(Matrix4fvArr o) {
        /*
         * Adapted from:
         * https://stackoverflow.com/questions/18499971/efficient-4x4-matrix-multiplication-c-vs-assembly#answer-18508113
         */
        FloatVector row1 = fromArray(SPECIES_128, o.es, 0);
        FloatVector row2 = fromArray(SPECIES_128, o.es, 4);
        FloatVector row3 = fromArray(SPECIES_128, o.es, 8);
        FloatVector row4 = fromArray(SPECIES_128, o.es, 12);
        for (int i = 0; i < 4; i++) {
            FloatVector r = fromArray(SPECIES_128, es, i<<2);
            // _mm_set1_ps(A[4*i + 0])
            FloatVector b0 = r.rearrange(s0000);
            // _mm_set1_ps(A[4*i + 1])
            FloatVector b1 = r.rearrange(s1111);
            // _mm_set1_ps(A[4*i + 2])
            FloatVector b2 = r.rearrange(s2222);
            // _mm_set1_ps(A[4*i + 3])
            FloatVector b3 = r.rearrange(s3333);
            b0.fma(row1, b1.fma(row2, b2.fma(row3, b3.mul(row4)))).intoArray(es, i<<2);
        }
        return this;
    }

    public Matrix4fvArr mul128Unrolled(Matrix4fvArr o) {
        /*
         * Adapted from:
         * https://stackoverflow.com/questions/18499971/efficient-4x4-matrix-multiplication-c-vs-assembly#answer-18508113
         */
        FloatVector row1 = fromArray(SPECIES_128, o.es, 0);
        FloatVector row2 = fromArray(SPECIES_128, o.es, 4);
        FloatVector row3 = fromArray(SPECIES_128, o.es, 8);
        FloatVector row4 = fromArray(SPECIES_128, o.es, 12);
        FloatVector r0, r1, r2, r3;
        r0 = fromArray(SPECIES_128, es, 0);
        r0.rearrange(s0000).fma(row1, r0.rearrange(s1111).fma(row2, r0.rearrange(s2222).fma(row3, r0.rearrange(s3333).mul(row4)))).intoArray(es, 0);
        r1 = fromArray(SPECIES_128, es, 4);
        r1.rearrange(s0000).fma(row1, r1.rearrange(s1111).fma(row2, r1.rearrange(s2222).fma(row3, r1.rearrange(s3333).mul(row4)))).intoArray(es, 4);
        r2 = fromArray(SPECIES_128, es, 8);
        r2.rearrange(s0000).fma(row1, r2.rearrange(s1111).fma(row2, r2.rearrange(s2222).fma(row3, r2.rearrange(s3333).mul(row4)))).intoArray(es, 8);
        r3 = fromArray(SPECIES_128, es, 12);
        r3.rearrange(s0000).fma(row1, r3.rearrange(s1111).fma(row2, r3.rearrange(s2222).fma(row3, r3.rearrange(s3333).mul(row4)))).intoArray(es, 12);
        return this;
    }

    public Matrix4fvArr mul256(Matrix4fvArr o) {
        /*
         * Adapted from:
         * https://stackoverflow.com/questions/19806222/matrix-vector-multiplication-in-avx-not-proportionately-faster-than-in-sse#answer-46058667
         */
        FloatVector t0 = fromArray(SPECIES_256, es, 0);
        FloatVector t1 = fromArray(SPECIES_256, es, 8);
        FloatVector u0 = fromArray(SPECIES_256, o.es, 0);
        FloatVector u1 = fromArray(SPECIES_256, o.es, 8);
        FloatVector u0r00 = u0.rearrange(s01230123);
        FloatVector u1r00 = u1.rearrange(s01230123);
        FloatVector u0r11 = u0.rearrange(s45674567);
        FloatVector u1r11 = u1.rearrange(s45674567);
        t0.rearrange(s00004444).fma(u0r00, t0.rearrange(s11115555).mul(u0r11))
                .add(t0.rearrange(s33337777).fma(u1r11, t0.rearrange(s22226666).mul(u1r00)))
                .intoArray(es, 0);
        t1.rearrange(s00004444).fma(u0r00, t1.rearrange(s11115555).mul(u0r11))
                .add(t1.rearrange(s33337777).fma(u1r11, t1.rearrange(s22226666).mul(u1r00)))
                .intoArray(es, 8);
        return this;
    }

    @Override
    public String toString() {
        DecimalFormat f = new DecimalFormat(" 0.000E0;-");
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                str.append(f.format(es[j * 4 + i]));
            }
            if (i < 3) {
                str.append("\n");
            }
        }
        return str.toString();
    }

    public ByteBuffer storeV256(ByteBuffer bb) {
        fromArray(SPECIES_256, es, 0).intoByteBuffer(bb, 0, nativeOrder());
        fromArray(SPECIES_256, es, 8).intoByteBuffer(bb, 32, nativeOrder());
        return bb;
    }

    public ByteBuffer storeU(ByteBuffer bb) {
        long addr = U.getLong(bb, A);
        for (int i = 0; i < 8; i++) {
            U.putLong(addr + (i << 3), U.getLong(es, O + (i << 3)));
        }
        return bb;
    }

    public static void main(String[] args) {
        System.out.println(new Matrix4fvArr().mul128Loop(new Matrix4fvArr()));
        System.out.println("----");
        System.out.println(new Matrix4fvArr().mul128Unrolled(new Matrix4fvArr()));
        System.out.println("----");
        System.out.println(new Matrix4fvArr().mul256(new Matrix4fvArr()));
    }

}
