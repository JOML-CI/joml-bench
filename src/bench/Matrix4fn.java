package bench;

import java.io.File;

/**
 * 4x4 matrix backed by 16-byte-aligned off-heap memory.
 */
public class Matrix4fn {
    static {
        System.load(new File("native/build/libjoml.so").getAbsolutePath());
    }

    private final long addr = allocate();

    private static native long allocate();
    private static native void free(long addr);
    private static native void mul(long m1, long m2, long dest);

    public Matrix4fn mul(Matrix4fn right) {
        mul(addr, right.addr, addr);
        return this;
    }
}
