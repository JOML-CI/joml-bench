package bench;

import java.io.File;

/**
 * 4x4 matrix backed by 32-byte-aligned off-heap memory.
 */
public class Matrix4fn {
    static {
        System.load(new File("native/build/libjoml.so").getAbsolutePath());
    }

    private final long addr = allocate();

    private static native long allocate();
    private static native void free(long addr);
    private static native void mulSSE(long m1, long m2, long dest);
    private static native void mulAVX(long m1, long m2, long dest);

    public Matrix4fn mulSSE(Matrix4fn right) {
        mulSSE(addr, right.addr, addr);
        return this;
    }
    public Matrix4fn mulAVX(Matrix4fn right) {
        mulAVX(addr, right.addr, addr);
        return this;
    }
}
