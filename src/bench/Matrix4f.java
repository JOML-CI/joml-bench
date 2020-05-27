package bench;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;

import static bench.Matrix4fv.A;
import static bench.Matrix4fv.U;

/**
 * 4x4 matrix backed by 16 float fields.
 */
public class Matrix4f {

    private static final long M00 = m00Offset();

    private static long m00Offset() {
        try {
            long m00 = U.objectFieldOffset(Matrix4f.class.getDeclaredField("m00"));
            for (int i = 1; i < 16; i++) {
                if (U.objectFieldOffset(Matrix4f.class.getDeclaredField("m" + (i>>>2) + "" + (i&3))) != m00 + (i<<2))
                    throw new AssertionError("unsupported offset");
            }
            return m00;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private float pad0; // <- to align m00 on 8 bytes for Unsafe.getLong()
    // column0
    private float m00, m01, m02, m03;
    // column1
    private float m10, m11, m12, m13;
    // column2
    private float m20, m21, m22, m23;
    // column3
    private float m30, m31, m32, m33;

    public Matrix4f() {
        // Initialize to identity.
        m00 = 1f;
        m11 = 1f;
        m22 = 1f;
        m33 = 1f;
    }

    /**
     * On CPUs with FMA3 support, this is usually slower than {@link #mulFma(Matrix4f)}.
     */
    public Matrix4f mul(Matrix4f right) {
        float nm00 = m00 * right.m00 + m10 * right.m01 + m20 * right.m02 + m30 * right.m03;
        float nm01 = m01 * right.m00 + m11 * right.m01 + m21 * right.m02 + m31 * right.m03;
        float nm02 = m02 * right.m00 + m12 * right.m01 + m22 * right.m02 + m32 * right.m03;
        float nm03 = m03 * right.m00 + m13 * right.m01 + m23 * right.m02 + m33 * right.m03;
        float nm10 = m00 * right.m10 + m10 * right.m11 + m20 * right.m12 + m30 * right.m13;
        float nm11 = m01 * right.m10 + m11 * right.m11 + m21 * right.m12 + m31 * right.m13;
        float nm12 = m02 * right.m10 + m12 * right.m11 + m22 * right.m12 + m32 * right.m13;
        float nm13 = m03 * right.m10 + m13 * right.m11 + m23 * right.m12 + m33 * right.m13;
        float nm20 = m00 * right.m20 + m10 * right.m21 + m20 * right.m22 + m30 * right.m23;
        float nm21 = m01 * right.m20 + m11 * right.m21 + m21 * right.m22 + m31 * right.m23;
        float nm22 = m02 * right.m20 + m12 * right.m21 + m22 * right.m22 + m32 * right.m23;
        float nm23 = m03 * right.m20 + m13 * right.m21 + m23 * right.m22 + m33 * right.m23;
        float nm30 = m00 * right.m30 + m10 * right.m31 + m20 * right.m32 + m30 * right.m33;
        float nm31 = m01 * right.m30 + m11 * right.m31 + m21 * right.m32 + m31 * right.m33;
        float nm32 = m02 * right.m30 + m12 * right.m31 + m22 * right.m32 + m32 * right.m33;
        float nm33 = m03 * right.m30 + m13 * right.m31 + m23 * right.m32 + m33 * right.m33;
        m00 = nm00;
        m01 = nm01;
        m02 = nm02;
        m03 = nm03;
        m10 = nm10;
        m11 = nm11;
        m12 = nm12;
        m13 = nm13;
        m20 = nm20;
        m21 = nm21;
        m22 = nm22;
        m23 = nm23;
        m30 = nm30;
        m31 = nm31;
        m32 = nm32;
        m33 = nm33;
        return this;
    }

    /**
     * ONLY EXECUTE THIS METHOD ON CPUs THAT SUPPORT FMA3!!!
     * <p>
     * Sadly, the JVM does not have any mechanisms for user-code to check whether the CPU does support it
     * in order to route a generalized mul() method to this mulFma() method only if it does. We have to rely
     * on JNI code querying CPUID for this...
     */
    public Matrix4f mulFma(Matrix4f right) {
        float nm00 = Math.fma(m00, right.m00, Math.fma(m10, right.m01, Math.fma(m20, right.m02, m30 * right.m03)));
        float nm01 = Math.fma(m01, right.m00, Math.fma(m11, right.m01, Math.fma(m21, right.m02, m31 * right.m03)));
        float nm02 = Math.fma(m02, right.m00, Math.fma(m12, right.m01, Math.fma(m22, right.m02, m32 * right.m03)));
        float nm03 = Math.fma(m03, right.m00, Math.fma(m13, right.m01, Math.fma(m23, right.m02, m33 * right.m03)));
        float nm10 = Math.fma(m00, right.m10, Math.fma(m10, right.m11, Math.fma(m20, right.m12, m30 * right.m13)));
        float nm11 = Math.fma(m01, right.m10, Math.fma(m11, right.m11, Math.fma(m21, right.m12, m31 * right.m13)));
        float nm12 = Math.fma(m02, right.m10, Math.fma(m12, right.m11, Math.fma(m22, right.m12, m32 * right.m13)));
        float nm13 = Math.fma(m03, right.m10, Math.fma(m13, right.m11, Math.fma(m23, right.m12, m33 * right.m13)));
        float nm20 = Math.fma(m00, right.m20, Math.fma(m10, right.m21, Math.fma(m20, right.m22, m30 * right.m23)));
        float nm21 = Math.fma(m01, right.m20, Math.fma(m11, right.m21, Math.fma(m21, right.m22, m31 * right.m23)));
        float nm22 = Math.fma(m02, right.m20, Math.fma(m12, right.m21, Math.fma(m22, right.m22, m32 * right.m23)));
        float nm23 = Math.fma(m03, right.m20, Math.fma(m13, right.m21, Math.fma(m23, right.m22, m33 * right.m23)));
        float nm30 = Math.fma(m00, right.m30, Math.fma(m10, right.m31, Math.fma(m20, right.m32, m30 * right.m33)));
        float nm31 = Math.fma(m01, right.m30, Math.fma(m11, right.m31, Math.fma(m21, right.m32, m31 * right.m33)));
        float nm32 = Math.fma(m02, right.m30, Math.fma(m12, right.m31, Math.fma(m22, right.m32, m32 * right.m33)));
        float nm33 = Math.fma(m03, right.m30, Math.fma(m13, right.m31, Math.fma(m23, right.m32, m33 * right.m33)));
        m00 = nm00;
        m01 = nm01;
        m02 = nm02;
        m03 = nm03;
        m10 = nm10;
        m11 = nm11;
        m12 = nm12;
        m13 = nm13;
        m20 = nm20;
        m21 = nm21;
        m22 = nm22;
        m23 = nm23;
        m30 = nm30;
        m31 = nm31;
        m32 = nm32;
        m33 = nm33;
        return this;
    }

    public Matrix4f mulAffineFma(Matrix4f right) {
        float nm00 = Math.fma(m00, right.m00, Math.fma(m10, right.m01, m20 * right.m02));
        float nm01 = Math.fma(m01, right.m00, Math.fma(m11, right.m01, m21 * right.m02));
        float nm02 = Math.fma(m02, right.m00, Math.fma(m12, right.m01, m22 * right.m02));
        float nm10 = Math.fma(m00, right.m10, Math.fma(m10, right.m11, m20 * right.m12));
        float nm11 = Math.fma(m01, right.m10, Math.fma(m11, right.m11, m21 * right.m12));
        float nm12 = Math.fma(m02, right.m10, Math.fma(m12, right.m11, m22 * right.m12));
        float nm20 = Math.fma(m00, right.m20, Math.fma(m10, right.m21, m20 * right.m22));
        float nm21 = Math.fma(m01, right.m20, Math.fma(m11, right.m21, m21 * right.m22));
        float nm22 = Math.fma(m02, right.m20, Math.fma(m12, right.m21, m22 * right.m22));
        float nm30 = Math.fma(m00, right.m30, Math.fma(m10, right.m31, Math.fma(m20, right.m32, m30)));
        float nm31 = Math.fma(m01, right.m30, Math.fma(m11, right.m31, Math.fma(m21, right.m32, m31)));
        float nm32 = Math.fma(m02, right.m30, Math.fma(m12, right.m31, Math.fma(m22, right.m32, m32)));
        m00 = nm00;
        m01 = nm01;
        m02 = nm02;
        m03 = 0.0f;
        m10 = nm10;
        m11 = nm11;
        m12 = nm12;
        m13 = 0.0f;
        m20 = nm20;
        m21 = nm21;
        m22 = nm22;
        m23 = 0.0f;
        m30 = nm30;
        m31 = nm31;
        m32 = nm32;
        m33 = 1.0f;
        return this;
    }

    public String toString() {
        DecimalFormat f = new DecimalFormat(" 0.000E0;-");
        return f.format(m00) + " " + f.format(m10) + " " + f.format(m20) + " " + f.format(m30) + "\n"
             + f.format(m01) + " " + f.format(m11) + " " + f.format(m21) + " " + f.format(m31) + "\n"
             + f.format(m02) + " " + f.format(m12) + " " + f.format(m22) + " " + f.format(m32) + "\n"
             + f.format(m03) + " " + f.format(m13) + " " + f.format(m23) + " " + f.format(m33);
    }

    public ByteBuffer storeU(ByteBuffer bb) {
        long addr = U.getLong(bb, A);
        for (int i = 0; i < 8; i++)
            U.putLong(addr + (i<<3), U.getLong(this, M00 + (i<<3)));
        return bb;
    }

    public static void main(String[] args) {
        System.out.println(new Matrix4f().mul(new Matrix4f()));
        System.out.println("----");
        System.out.println(new Matrix4f().mulFma(new Matrix4f()));
    }

}
