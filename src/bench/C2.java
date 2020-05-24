package bench;

public class C2 {
    private static final int COUNT = 10000000;

    public static void main(String[] args) {
        mulScalar();
        mulScalarFma();
        mul128Loop();
        mul128Unrolled();
        mul256();
    }

    private static void mulScalar() {
        Matrix4f m4 = new Matrix4f();
        for (int i = 0; i < COUNT; i++)
            m4.mul(m4);
    }

    private static void mulScalarFma() {
        Matrix4f m4 = new Matrix4f();
        for (int i = 0; i < COUNT; i++)
            m4.mulFma(m4);
    }

    private static void mul128Loop() {
        Matrix4fvBB m4vbb = new Matrix4fvBB();
        for (int i = 0; i < COUNT; i++)
            m4vbb.mul128Loop(m4vbb);
    }

    private static void mul128Unrolled() {
        Matrix4fvBB m4vbb = new Matrix4fvBB();
        for (int i = 0; i < COUNT; i++)
            m4vbb = m4vbb.mul128Unrolled(m4vbb);
    }

    private static void mul256() {
        Matrix4fvBB m4vbb = new Matrix4fvBB();
        for (int i = 0; i < COUNT; i++)
            m4vbb.mul256(m4vbb);
    }
}
