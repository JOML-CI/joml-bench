package bench;

import jdk.incubator.vector.FloatVector;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
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

    public Matrix4fvArr invert128(Matrix4fvArr dest) {
        /*
         * Adapted from: https://github.com/niswegmann/small-matrix-inverse/blob/master/invert4x4_llvm.h
         */
        FloatVector col0 = fromArray(SPECIES_128, es, 0);
        FloatVector col1 = fromArray(SPECIES_128, es, 4);
        FloatVector col2 = fromArray(SPECIES_128, es, 8);
        FloatVector col3 = fromArray(SPECIES_128, es, 12);
        // tmp1 = __builtin_shufflevector(col0, col2, 0, 4, 1, 5);
        FloatVector tmp1 = col0.rearrange(s0415, col2);
        // row1 = __builtin_shufflevector(col1, col3, 0, 4, 1, 5);
        FloatVector row1 = col1.rearrange(s0415, col3);
        // row0 = __builtin_shufflevector(tmp1, row1, 0, 4, 1, 5);
        FloatVector row0 = tmp1.rearrange(s0415, row1);
        // row1 = __builtin_shufflevector(tmp1, row1, 2, 6, 3, 7);
        row1 = tmp1.rearrange(s2637, row1);
        // tmp1 = __builtin_shufflevector(col0, col2, 2, 6, 3, 7);
        tmp1 = col0.rearrange(s2637, col2);
        // row3 = __builtin_shufflevector(col1, col3, 2, 6, 3, 7);
        FloatVector row3 = col1.rearrange(s2637, col3);
        // row2 = __builtin_shufflevector(tmp1, row3, 0, 4, 1, 5);
        FloatVector row2 = tmp1.rearrange(s0415, row3);
        // row3 = __builtin_shufflevector(tmp1, row3, 2, 6, 3, 7);
        row3 = tmp1.rearrange(s2637, row3);
        // row1 = __builtin_shufflevector(row1, row1, 2, 3, 0, 1);
        row1 = row1.rearrange(s2301);
        // row3 = __builtin_shufflevector(row3, row3, 2, 3, 0, 1);
        row3 = row3.rearrange(s2301);
        // tmp1 = row2 * row3;
        tmp1 = row2.mul(row3);
        // tmp1 = __builtin_shufflevector(tmp1, tmp1, 1, 0, 7, 6);
        tmp1 = tmp1.rearrange(s1032);
        // col0 = row1 * tmp1;
        col0 = row1.mul(tmp1);
        // col1 = row0 * tmp1;
        col1 = row0.mul(tmp1);
        // tmp1 = __builtin_shufflevector(tmp1, tmp1, 2, 3, 4, 5);
        tmp1 = tmp1.rearrange(s2301);
        // col0 = row1 * tmp1 - col0;
        col0 = row1.fma(tmp1, col0.neg());
        // col1 = row0 * tmp1 - col1;
        col1 = row0.fma(tmp1, col1.neg());
        // col1 = __builtin_shufflevector(col1, col1, 2, 3, 4, 5);
        col1 = col1.rearrange(s2301);
        // tmp1 = row1 * row2;
        tmp1 = row1.mul(row2);
        // tmp1 = __builtin_shufflevector(tmp1, tmp1, 1, 0, 7, 6);
        tmp1 = tmp1.rearrange(s1032);
        // col0 = row3 * tmp1 + col0;
        col0 = row3.fma(tmp1, col0);
        // col3 = row0 * tmp1;
        col3 = row0.mul(tmp1);
        // tmp1 = __builtin_shufflevector(tmp1, tmp1, 2, 3, 4, 5);
        tmp1 = tmp1.rearrange(s2301);
        // col0 = col0 - row3 * tmp1;
        col0 = col0.sub(row3.mul(tmp1));
        // col3 = row0 * tmp1 - col3;
        col3 = row0.fma(tmp1, col3.neg());
        // col3 = __builtin_shufflevector(col3, col3, 2, 3, 4, 5);
        col3 = col3.rearrange(s2301);
        // tmp1 = __builtin_shufflevector(row1, row1, 2, 3, 4, 5) * row3;
        tmp1 = row1.rearrange(s2301).mul(row3);
        // tmp1 = __builtin_shufflevector(tmp1, tmp1, 1, 0, 7, 6);
        tmp1 = tmp1.rearrange(s1032);
        // row2 = __builtin_shufflevector(row2, row2, 2, 3, 4, 5);
        row2 = row2.rearrange(s2301);
        // col0 = row2 * tmp1 + col0;
        col0 = row2.fma(tmp1, col0);
        // col2 = row0 * tmp1;
        col2 = row0.mul(tmp1);
        // tmp1 = __builtin_shufflevector(tmp1, tmp1, 2, 3, 4, 5);
        tmp1 = tmp1.rearrange(s2301);
        // col0 = col0 - row2 * tmp1;
        col0 = col0.sub(row2.mul(tmp1));
        // col2 = row0 * tmp1 - col2;
        col2 = row0.fma(tmp1, col2.neg());
        // col2 = __builtin_shufflevector(col2, col2, 2, 3, 4, 5);
        col2 = col2.rearrange(s2301);
        // tmp1 = row0 * row1;
        tmp1 = row0.mul(row1);
        // tmp1 = __builtin_shufflevector(tmp1, tmp1, 1, 0, 7, 6);
        tmp1 = tmp1.rearrange(s1032);
        // col2 = row3 * tmp1 + col2;
        col2 = row3.fma(tmp1, col2);
        // col3 = row2 * tmp1 - col3;
        col3 = row2.fma(tmp1, col3.neg());
        // tmp1 = __builtin_shufflevector(tmp1, tmp1, 2, 3, 4, 5);
        tmp1 = tmp1.rearrange(s2301);
        // col2 = row3 * tmp1 - col2;
        col2 = row3.fma(tmp1, col2.neg());
        // col3 = col3 - row2 * tmp1;
        col3 = col3.sub(row2.mul(tmp1));
        // tmp1 = row0 * row3;
        tmp1 = row0.mul(row3);
        // tmp1 = __builtin_shufflevector(tmp1, tmp1, 1, 0, 7, 6);
        tmp1 = tmp1.rearrange(s1032);
        // col1 = col1 - row2 * tmp1;
        col1 = col1.sub(row2.mul(tmp1));
        // col2 = row1 * tmp1 + col2;
        col2 = row1.fma(tmp1, col2);
        // tmp1 = __builtin_shufflevector(tmp1, tmp1, 2, 3, 4, 5);
        tmp1 = tmp1.rearrange(s2301);
        // col1 = row2 * tmp1 + col1;
        col1 = row2.fma(tmp1, col1);
        // col2 = col2 - row1 * tmp1;
        col2 = col2.sub(row1.mul(tmp1));
        // tmp1 = row0 * row2;
        tmp1 = row0.mul(row2);
        // tmp1 = __builtin_shufflevector(tmp1, tmp1, 1, 0, 7, 6);
        tmp1 = tmp1.rearrange(s1032);
        // col1 = row3 * tmp1 + col1;
        col1 = row3.fma(tmp1, col1);
        // col3 = col3 - row1 * tmp1;
        col3 = col3.sub(row1.mul(tmp1));
        // tmp1 = __builtin_shufflevector(tmp1, tmp1, 2, 3, 4, 5);
        tmp1 = tmp1.rearrange(s2301);
        // col1 = col1 - row3 * tmp1;
        col1 = col1.sub(row3.mul(tmp1));
        // col3 = row1 * tmp1 + col3;
        col3 = row1.fma(tmp1, col3);
        // det = row0 * col0;
        FloatVector det = row0.mul(col0);
        // det = __builtin_shufflevector(det, det, 2, 3, 4, 5) + det;
        det = det.rearrange(s2301).add(det);
        // det = __builtin_shufflevector(det, det, 1, 0, 7, 6) + det;
        det = det.rearrange(s1032).add(det);
        // det = 1.0f / det;
        det = broadcast(SPECIES_128, 1.0f).div(det);
        // col0 = col0 * det;
        col0.mul(det).intoArray(dest.es, 0);
        // col1 = col1 * det;
        col1.mul(det).intoArray(dest.es, 4);
        // col2 = col2 * det;
        col2.mul(det).intoArray(dest.es, 8);
        // col3 = col3 * det;
        col3.mul(det).intoArray(dest.es, 12);
        return dest;
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

    public FloatBuffer storePut(FloatBuffer fb) {
        fb.put(0, es, 0, 16);
        return fb;
    }

    public ByteBuffer storeV256(ByteBuffer bb) {
        fromArray(SPECIES_256, es, 0).intoByteBuffer(bb, 0, nativeOrder());
        fromArray(SPECIES_256, es, 8).intoByteBuffer(bb, 32, nativeOrder());
        return bb;
    }

    public ByteBuffer storeV512(ByteBuffer bb) {
        fromArray(SPECIES_512, es, 0).intoByteBuffer(bb, 0, nativeOrder());
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
