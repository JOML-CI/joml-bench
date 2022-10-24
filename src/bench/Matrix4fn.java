package bench;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.JAVA_LONG;

/**
 * 4x4 matrix backed by 32-byte-aligned off-heap memory.
 */
public class Matrix4fn {
    static {
        System.loadLibrary("joml");
    }

    private final long addr = allocate();

    private static native long allocate();
    private static native void free(long addr);
    private static native void noop2(long m1, long m2);
    private static native void mulSSE(long m1, long m2, long dest);
    private static native void mulAVX(long m1, long m2, long dest);

    public static final MethodHandle noop2ForPanama = getNoopForPanama();
    private static MethodHandle getNoopForPanama() {
        return Linker.nativeLinker().downcallHandle(
                SymbolLookup.loaderLookup().lookup("noop2ForPanama").get(),
                FunctionDescriptor.ofVoid(JAVA_LONG, JAVA_LONG)
        );
    }

    public Matrix4fn noop(Matrix4fn right) {
        noop2(addr, right.addr);
        return this;
    }
    public Matrix4fn mulSSE(Matrix4fn right) {
        mulSSE(addr, right.addr, addr);
        return this;
    }
    public Matrix4fn mulAVX(Matrix4fn right) {
        mulAVX(addr, right.addr, addr);
        return this;
    }
}
