package bench;

import jdk.vm.ci.code.site.DataPatch;
import jdk.vm.ci.code.site.Site;
import jdk.vm.ci.hotspot.HotSpotCompiledCode;
import jdk.vm.ci.hotspot.HotSpotCompiledNmethod;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod;
import jdk.vm.ci.meta.Assumptions;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.runtime.JVMCI;
import jdk.vm.ci.runtime.JVMCIBackend;
import jdk.vm.ci.runtime.JVMCICompiler;

import java.lang.reflect.Method;

public class WithJvmci {

  private static final byte[] INVERT_LINUX = {
          (byte) 0xC5, (byte) 0xFB, (byte) 0x10, (byte) 0x46, (byte) 0x04, (byte) 0xC5,
          (byte) 0xFB, (byte) 0x10, (byte) 0x4E, (byte) 0x0C, (byte) 0xC5, (byte) 0xFB, (byte) 0x10, (byte) 0x5E,
          (byte) 0x24, (byte) 0xC5, (byte) 0xF8, (byte) 0x16, (byte) 0x56, (byte) 0x14, (byte) 0xC5, (byte) 0xE0,
          (byte) 0x16, (byte) 0x66, (byte) 0x34, (byte) 0xC5, (byte) 0xFB, (byte) 0x10, (byte) 0x5E, (byte) 0x2C,
          (byte) 0xC5, (byte) 0xE8, (byte) 0xC6, (byte) 0xC4, (byte) 0x88, (byte) 0xC5, (byte) 0xF0, (byte) 0x16,
          (byte) 0x6E, (byte) 0x1C, (byte) 0xC5, (byte) 0xD8, (byte) 0xC6, (byte) 0xCA, (byte) 0xDD, (byte) 0xC5,
          (byte) 0xE0, (byte) 0x16, (byte) 0x76, (byte) 0x3C, (byte) 0xC5, (byte) 0xD0, (byte) 0xC6, (byte) 0xFE,
          (byte) 0x88, (byte) 0xC5, (byte) 0xC8, (byte) 0xC6, (byte) 0xDD, (byte) 0xDD, (byte) 0xC5, (byte) 0x40,
          (byte) 0x59, (byte) 0xC3, (byte) 0xC4, (byte) 0x43, (byte) 0x79, (byte) 0x04, (byte) 0xC0, (byte) 0xB1,
          (byte) 0xC5, (byte) 0x38, (byte) 0x59, (byte) 0xC9, (byte) 0xC5, (byte) 0x38, (byte) 0x59, (byte) 0xD0,
          (byte) 0xC4, (byte) 0x43, (byte) 0x79, (byte) 0x05, (byte) 0xC0, (byte) 0x01, (byte) 0xC5, (byte) 0x38,
          (byte) 0x59, (byte) 0xD9, (byte) 0xC4, (byte) 0x41, (byte) 0x20, (byte) 0x5C, (byte) 0xC9, (byte) 0xC5,
          (byte) 0x38, (byte) 0x59, (byte) 0xC0, (byte) 0xC4, (byte) 0x41, (byte) 0x38, (byte) 0x5C, (byte) 0xC2,
          (byte) 0xC4, (byte) 0x43, (byte) 0x79, (byte) 0x05, (byte) 0xC0, (byte) 0x01, (byte) 0xC5, (byte) 0x70,
          (byte) 0x59, (byte) 0xD7, (byte) 0xC4, (byte) 0x43, (byte) 0x79, (byte) 0x04, (byte) 0xD2, (byte) 0xB1,
          (byte) 0xC5, (byte) 0x28, (byte) 0x59, (byte) 0xDB, (byte) 0xC4, (byte) 0x41, (byte) 0x20, (byte) 0x58,
          (byte) 0xC9, (byte) 0xC5, (byte) 0x28, (byte) 0x59, (byte) 0xD8, (byte) 0xC4, (byte) 0x43, (byte) 0x79,
          (byte) 0x05, (byte) 0xD2, (byte) 0x01, (byte) 0xC5, (byte) 0x28, (byte) 0x59, (byte) 0xE3, (byte) 0xC4,
          (byte) 0x41, (byte) 0x30, (byte) 0x5C, (byte) 0xCC, (byte) 0xC5, (byte) 0x28, (byte) 0x59, (byte) 0xD0,
          (byte) 0xC4, (byte) 0x41, (byte) 0x28, (byte) 0x5C, (byte) 0xD3, (byte) 0xC4, (byte) 0x43, (byte) 0x79,
          (byte) 0x05, (byte) 0xD2, (byte) 0x01, (byte) 0xC5, (byte) 0x48, (byte) 0xC6, (byte) 0xDD, (byte) 0x77,
          (byte) 0xC5, (byte) 0x68, (byte) 0xC6, (byte) 0xE4, (byte) 0x77, (byte) 0xC4, (byte) 0x41, (byte) 0x18,
          (byte) 0x59, (byte) 0xDB, (byte) 0xC4, (byte) 0x63, (byte) 0x79, (byte) 0x05, (byte) 0xE7, (byte) 0x01,
          (byte) 0xC4, (byte) 0xC1, (byte) 0x18, (byte) 0x59, (byte) 0xFB, (byte) 0xC5, (byte) 0xB0, (byte) 0x58,
          (byte) 0xFF, (byte) 0xC5, (byte) 0x20, (byte) 0x59, (byte) 0xC8, (byte) 0xC4, (byte) 0x43, (byte) 0x79,
          (byte) 0x05, (byte) 0xDB, (byte) 0x01, (byte) 0xC4, (byte) 0x41, (byte) 0x18, (byte) 0x59, (byte) 0xEB,
          (byte) 0xC4, (byte) 0xC1, (byte) 0x40, (byte) 0x5C, (byte) 0xFD, (byte) 0xC5, (byte) 0x20, (byte) 0x59,
          (byte) 0xD8, (byte) 0xC4, (byte) 0x41, (byte) 0x20, (byte) 0x5C, (byte) 0xC9, (byte) 0xC4, (byte) 0x43,
          (byte) 0x79, (byte) 0x05, (byte) 0xC9, (byte) 0x01, (byte) 0xC5, (byte) 0x78, (byte) 0x59, (byte) 0xD9,
          (byte) 0xC4, (byte) 0x43, (byte) 0x79, (byte) 0x04, (byte) 0xDB, (byte) 0xB1, (byte) 0xC5, (byte) 0x20,
          (byte) 0x59, (byte) 0xEB, (byte) 0xC4, (byte) 0x41, (byte) 0x10, (byte) 0x58, (byte) 0xC9, (byte) 0xC4,
          (byte) 0x41, (byte) 0x20, (byte) 0x59, (byte) 0xEC, (byte) 0xC4, (byte) 0x41, (byte) 0x10, (byte) 0x5C,
          (byte) 0xD2, (byte) 0xC4, (byte) 0x43, (byte) 0x79, (byte) 0x05, (byte) 0xDB, (byte) 0x01, (byte) 0xC5,
          (byte) 0x20, (byte) 0x59, (byte) 0xEB, (byte) 0xC4, (byte) 0x41, (byte) 0x10, (byte) 0x5C, (byte) 0xC9,
          (byte) 0xC4, (byte) 0x41, (byte) 0x20, (byte) 0x59, (byte) 0xDC, (byte) 0xC4, (byte) 0x41, (byte) 0x28,
          (byte) 0x5C, (byte) 0xD3, (byte) 0xC5, (byte) 0x78, (byte) 0x59, (byte) 0xDB, (byte) 0xC4, (byte) 0x43,
          (byte) 0x79, (byte) 0x04, (byte) 0xDB, (byte) 0xB1, (byte) 0xC4, (byte) 0x41, (byte) 0x18, (byte) 0x59,
          (byte) 0xEB, (byte) 0xC4, (byte) 0x41, (byte) 0x38, (byte) 0x5C, (byte) 0xC5, (byte) 0xC5, (byte) 0x20,
          (byte) 0x59, (byte) 0xE9, (byte) 0xC4, (byte) 0x41, (byte) 0x10, (byte) 0x58, (byte) 0xC9, (byte) 0xC4,
          (byte) 0x43, (byte) 0x79, (byte) 0x05, (byte) 0xDB, (byte) 0x01, (byte) 0xC4, (byte) 0x41, (byte) 0x18,
          (byte) 0x59, (byte) 0xE3, (byte) 0xC4, (byte) 0x41, (byte) 0x18, (byte) 0x58, (byte) 0xC0, (byte) 0xC5,
          (byte) 0x20, (byte) 0x59, (byte) 0xD9, (byte) 0xC4, (byte) 0x41, (byte) 0x30, (byte) 0x5C, (byte) 0xCB,
          (byte) 0xC5, (byte) 0xC8, (byte) 0xC6, (byte) 0xED, (byte) 0x22, (byte) 0xC5, (byte) 0xE8, (byte) 0xC6,
          (byte) 0xD4, (byte) 0x22, (byte) 0xC5, (byte) 0xE8, (byte) 0x59, (byte) 0xD5, (byte) 0xC5, (byte) 0xE0,
          (byte) 0x59, (byte) 0xE2, (byte) 0xC5, (byte) 0xB8, (byte) 0x58, (byte) 0xE4, (byte) 0xC5, (byte) 0xF0,
          (byte) 0x59, (byte) 0xEA, (byte) 0xC5, (byte) 0xA8, (byte) 0x5C, (byte) 0xED, (byte) 0xC4, (byte) 0xE3,
          (byte) 0x79, (byte) 0x05, (byte) 0xD2, (byte) 0x01, (byte) 0xC5, (byte) 0xE0, (byte) 0x59, (byte) 0xDA,
          (byte) 0xC5, (byte) 0xD8, (byte) 0x5C, (byte) 0xDB, (byte) 0xC5, (byte) 0xF0, (byte) 0x59, (byte) 0xCA,
          (byte) 0xC5, (byte) 0xF0, (byte) 0x58, (byte) 0xCD, (byte) 0xC5, (byte) 0xF8, (byte) 0x59, (byte) 0xC7,
          (byte) 0xC4, (byte) 0xE3, (byte) 0x79, (byte) 0x05, (byte) 0xD0, (byte) 0x01, (byte) 0xC5, (byte) 0xF8,
          (byte) 0x58, (byte) 0xC2, (byte) 0xC5, (byte) 0xFA, (byte) 0x16, (byte) 0xD0, (byte) 0xC5, (byte) 0xE8,
          (byte) 0x58, (byte) 0xC0, (byte) 0xC5, (byte) 0xFA, (byte) 0x53, (byte) 0xD0, (byte) 0xC5, (byte) 0xEA,
          (byte) 0x59, (byte) 0xE2, (byte) 0xC5, (byte) 0xDA, (byte) 0x59, (byte) 0xC0, (byte) 0xC5, (byte) 0xEA,
          (byte) 0x58, (byte) 0xD2, (byte) 0xC5, (byte) 0xEA, (byte) 0x5C, (byte) 0xC0, (byte) 0xC4, (byte) 0xE3,
          (byte) 0x79, (byte) 0x04, (byte) 0xC0, (byte) 0x00, (byte) 0xC5, (byte) 0xC0, (byte) 0x59, (byte) 0xD0,
          (byte) 0xC5, (byte) 0xE0, (byte) 0x59, (byte) 0xD8, (byte) 0xC5, (byte) 0xF8, (byte) 0x11, (byte) 0x5A,
          (byte) 0x14, (byte) 0xC5, (byte) 0xF8, (byte) 0x11, (byte) 0x52, (byte) 0x04, (byte) 0xC5, (byte) 0xB0,
          (byte) 0x59, (byte) 0xD0, (byte) 0xC5, (byte) 0xF0, (byte) 0x59, (byte) 0xC0, (byte) 0xC5, (byte) 0xF8,
          (byte) 0x11, (byte) 0x42, (byte) 0x34, (byte) 0xC5, (byte) 0xF8, (byte) 0x11, (byte) 0x52, (byte) 0x24,
          (byte) 0xC3 };
  private static final byte[] INVERT_WINDOWS = {
          (byte) 0xC5, (byte) 0xFB, (byte) 0x10, (byte) 0x42, (byte) 0x04, (byte) 0xC5,
          (byte) 0xFB, (byte) 0x10, (byte) 0x4A, (byte) 0x0C, (byte) 0xC5, (byte) 0xFB, (byte) 0x10, (byte) 0x5A,
          (byte) 0x24, (byte) 0xC5, (byte) 0xF8, (byte) 0x16, (byte) 0x52, (byte) 0x14, (byte) 0xC5, (byte) 0xE0,
          (byte) 0x16, (byte) 0x62, (byte) 0x34, (byte) 0xC5, (byte) 0xFB, (byte) 0x10, (byte) 0x5A, (byte) 0x2C,
          (byte) 0xC5, (byte) 0xE8, (byte) 0xC6, (byte) 0xC4, (byte) 0x88, (byte) 0xC5, (byte) 0xF0, (byte) 0x16,
          (byte) 0x6A, (byte) 0x1C, (byte) 0xC5, (byte) 0xD8, (byte) 0xC6, (byte) 0xCA, (byte) 0xDD, (byte) 0xC5,
          (byte) 0xE0, (byte) 0x16, (byte) 0x72, (byte) 0x3C, (byte) 0xC5, (byte) 0xD0, (byte) 0xC6, (byte) 0xFE,
          (byte) 0x88, (byte) 0xC5, (byte) 0xC8, (byte) 0xC6, (byte) 0xDD, (byte) 0xDD, (byte) 0xC5, (byte) 0x40,
          (byte) 0x59, (byte) 0xC3, (byte) 0xC4, (byte) 0x43, (byte) 0x79, (byte) 0x04, (byte) 0xC0, (byte) 0xB1,
          (byte) 0xC5, (byte) 0x38, (byte) 0x59, (byte) 0xC9, (byte) 0xC5, (byte) 0x38, (byte) 0x59, (byte) 0xD0,
          (byte) 0xC4, (byte) 0x43, (byte) 0x79, (byte) 0x05, (byte) 0xC0, (byte) 0x01, (byte) 0xC5, (byte) 0x38,
          (byte) 0x59, (byte) 0xD9, (byte) 0xC4, (byte) 0x41, (byte) 0x20, (byte) 0x5C, (byte) 0xC9, (byte) 0xC5,
          (byte) 0x38, (byte) 0x59, (byte) 0xC0, (byte) 0xC4, (byte) 0x41, (byte) 0x38, (byte) 0x5C, (byte) 0xC2,
          (byte) 0xC4, (byte) 0x43, (byte) 0x79, (byte) 0x05, (byte) 0xC0, (byte) 0x01, (byte) 0xC5, (byte) 0x70,
          (byte) 0x59, (byte) 0xD7, (byte) 0xC4, (byte) 0x43, (byte) 0x79, (byte) 0x04, (byte) 0xD2, (byte) 0xB1,
          (byte) 0xC5, (byte) 0x28, (byte) 0x59, (byte) 0xDB, (byte) 0xC4, (byte) 0x41, (byte) 0x20, (byte) 0x58,
          (byte) 0xC9, (byte) 0xC5, (byte) 0x28, (byte) 0x59, (byte) 0xD8, (byte) 0xC4, (byte) 0x43, (byte) 0x79,
          (byte) 0x05, (byte) 0xD2, (byte) 0x01, (byte) 0xC5, (byte) 0x28, (byte) 0x59, (byte) 0xE3, (byte) 0xC4,
          (byte) 0x41, (byte) 0x30, (byte) 0x5C, (byte) 0xCC, (byte) 0xC5, (byte) 0x28, (byte) 0x59, (byte) 0xD0,
          (byte) 0xC4, (byte) 0x41, (byte) 0x28, (byte) 0x5C, (byte) 0xD3, (byte) 0xC4, (byte) 0x43, (byte) 0x79,
          (byte) 0x05, (byte) 0xD2, (byte) 0x01, (byte) 0xC5, (byte) 0x48, (byte) 0xC6, (byte) 0xDD, (byte) 0x77,
          (byte) 0xC5, (byte) 0x68, (byte) 0xC6, (byte) 0xE4, (byte) 0x77, (byte) 0xC4, (byte) 0x41, (byte) 0x18,
          (byte) 0x59, (byte) 0xDB, (byte) 0xC4, (byte) 0x63, (byte) 0x79, (byte) 0x05, (byte) 0xE7, (byte) 0x01,
          (byte) 0xC4, (byte) 0xC1, (byte) 0x18, (byte) 0x59, (byte) 0xFB, (byte) 0xC5, (byte) 0xB0, (byte) 0x58,
          (byte) 0xFF, (byte) 0xC5, (byte) 0x20, (byte) 0x59, (byte) 0xC8, (byte) 0xC4, (byte) 0x43, (byte) 0x79,
          (byte) 0x05, (byte) 0xDB, (byte) 0x01, (byte) 0xC4, (byte) 0x41, (byte) 0x18, (byte) 0x59, (byte) 0xEB,
          (byte) 0xC4, (byte) 0xC1, (byte) 0x40, (byte) 0x5C, (byte) 0xFD, (byte) 0xC5, (byte) 0x20, (byte) 0x59,
          (byte) 0xD8, (byte) 0xC4, (byte) 0x41, (byte) 0x20, (byte) 0x5C, (byte) 0xC9, (byte) 0xC4, (byte) 0x43,
          (byte) 0x79, (byte) 0x05, (byte) 0xC9, (byte) 0x01, (byte) 0xC5, (byte) 0x78, (byte) 0x59, (byte) 0xD9,
          (byte) 0xC4, (byte) 0x43, (byte) 0x79, (byte) 0x04, (byte) 0xDB, (byte) 0xB1, (byte) 0xC5, (byte) 0x20,
          (byte) 0x59, (byte) 0xEB, (byte) 0xC4, (byte) 0x41, (byte) 0x10, (byte) 0x58, (byte) 0xC9, (byte) 0xC4,
          (byte) 0x41, (byte) 0x20, (byte) 0x59, (byte) 0xEC, (byte) 0xC4, (byte) 0x41, (byte) 0x10, (byte) 0x5C,
          (byte) 0xD2, (byte) 0xC4, (byte) 0x43, (byte) 0x79, (byte) 0x05, (byte) 0xDB, (byte) 0x01, (byte) 0xC5,
          (byte) 0x20, (byte) 0x59, (byte) 0xEB, (byte) 0xC4, (byte) 0x41, (byte) 0x10, (byte) 0x5C, (byte) 0xC9,
          (byte) 0xC4, (byte) 0x41, (byte) 0x20, (byte) 0x59, (byte) 0xDC, (byte) 0xC4, (byte) 0x41, (byte) 0x28,
          (byte) 0x5C, (byte) 0xD3, (byte) 0xC5, (byte) 0x78, (byte) 0x59, (byte) 0xDB, (byte) 0xC4, (byte) 0x43,
          (byte) 0x79, (byte) 0x04, (byte) 0xDB, (byte) 0xB1, (byte) 0xC4, (byte) 0x41, (byte) 0x18, (byte) 0x59,
          (byte) 0xEB, (byte) 0xC4, (byte) 0x41, (byte) 0x38, (byte) 0x5C, (byte) 0xC5, (byte) 0xC5, (byte) 0x20,
          (byte) 0x59, (byte) 0xE9, (byte) 0xC4, (byte) 0x41, (byte) 0x10, (byte) 0x58, (byte) 0xC9, (byte) 0xC4,
          (byte) 0x43, (byte) 0x79, (byte) 0x05, (byte) 0xDB, (byte) 0x01, (byte) 0xC4, (byte) 0x41, (byte) 0x18,
          (byte) 0x59, (byte) 0xE3, (byte) 0xC4, (byte) 0x41, (byte) 0x18, (byte) 0x58, (byte) 0xC0, (byte) 0xC5,
          (byte) 0x20, (byte) 0x59, (byte) 0xD9, (byte) 0xC4, (byte) 0x41, (byte) 0x30, (byte) 0x5C, (byte) 0xCB,
          (byte) 0xC5, (byte) 0xC8, (byte) 0xC6, (byte) 0xED, (byte) 0x22, (byte) 0xC5, (byte) 0xE8, (byte) 0xC6,
          (byte) 0xD4, (byte) 0x22, (byte) 0xC5, (byte) 0xE8, (byte) 0x59, (byte) 0xD5, (byte) 0xC5, (byte) 0xE0,
          (byte) 0x59, (byte) 0xE2, (byte) 0xC5, (byte) 0xB8, (byte) 0x58, (byte) 0xE4, (byte) 0xC5, (byte) 0xF0,
          (byte) 0x59, (byte) 0xEA, (byte) 0xC5, (byte) 0xA8, (byte) 0x5C, (byte) 0xED, (byte) 0xC4, (byte) 0xE3,
          (byte) 0x79, (byte) 0x05, (byte) 0xD2, (byte) 0x01, (byte) 0xC5, (byte) 0xE0, (byte) 0x59, (byte) 0xDA,
          (byte) 0xC5, (byte) 0xD8, (byte) 0x5C, (byte) 0xDB, (byte) 0xC5, (byte) 0xF0, (byte) 0x59, (byte) 0xCA,
          (byte) 0xC5, (byte) 0xF0, (byte) 0x58, (byte) 0xCD, (byte) 0xC5, (byte) 0xF8, (byte) 0x59, (byte) 0xC7,
          (byte) 0xC4, (byte) 0xE3, (byte) 0x79, (byte) 0x05, (byte) 0xD0, (byte) 0x01, (byte) 0xC5, (byte) 0xF8,
          (byte) 0x58, (byte) 0xC2, (byte) 0xC5, (byte) 0xFA, (byte) 0x16, (byte) 0xD0, (byte) 0xC5, (byte) 0xE8,
          (byte) 0x58, (byte) 0xC0, (byte) 0xC5, (byte) 0xFA, (byte) 0x53, (byte) 0xD0, (byte) 0xC5, (byte) 0xEA,
          (byte) 0x59, (byte) 0xE2, (byte) 0xC5, (byte) 0xDA, (byte) 0x59, (byte) 0xC0, (byte) 0xC5, (byte) 0xEA,
          (byte) 0x58, (byte) 0xD2, (byte) 0xC5, (byte) 0xEA, (byte) 0x5C, (byte) 0xC0, (byte) 0xC4, (byte) 0xE3,
          (byte) 0x79, (byte) 0x04, (byte) 0xC0, (byte) 0x00, (byte) 0xC5, (byte) 0xC0, (byte) 0x59, (byte) 0xD0,
          (byte) 0xC5, (byte) 0xE0, (byte) 0x59, (byte) 0xD8, (byte) 0xC4, (byte) 0xC1, (byte) 0x78, (byte) 0x11,
          (byte) 0x58, (byte) 0x14, (byte) 0xC4, (byte) 0xC1, (byte) 0x78, (byte) 0x11, (byte) 0x50, (byte) 0x04,
          (byte) 0xC5, (byte) 0xB0, (byte) 0x59, (byte) 0xD0, (byte) 0xC5, (byte) 0xF0, (byte) 0x59, (byte) 0xC0,
          (byte) 0xC4, (byte) 0xC1, (byte) 0x78, (byte) 0x11, (byte) 0x40, (byte) 0x34, (byte) 0xC4, (byte) 0xC1,
          (byte) 0x78, (byte) 0x11, (byte) 0x50, (byte) 0x24, (byte) 0xC3 };

  private static final byte[] MUL_LINUX = {
          (byte) 0xC5, (byte) 0xF8, (byte) 0x10, (byte) 0x56, (byte) 0x10, (byte) 0xC5, (byte) 0xF8, (byte) 0x10,
          (byte) 0x5E, (byte) 0x20, (byte) 0xC5, (byte) 0xF8, (byte) 0x10, (byte) 0x46, (byte) 0x30, (byte) 0xC5,
          (byte) 0xF8, (byte) 0x10, (byte) 0x4E, (byte) 0x40, (byte) 0xC4, (byte) 0xE2, (byte) 0x79, (byte) 0x18,
          (byte) 0x62, (byte) 0x10, (byte) 0xC4, (byte) 0xE2, (byte) 0x79, (byte) 0x18, (byte) 0x6A, (byte) 0x14,
          (byte) 0xC4, (byte) 0xE2, (byte) 0x79, (byte) 0x18, (byte) 0x72, (byte) 0x18, (byte) 0xC4, (byte) 0xE2,
          (byte) 0x79, (byte) 0x18, (byte) 0x7A, (byte) 0x1C, (byte) 0xC5, (byte) 0xE8, (byte) 0x59, (byte) 0xE4,
          (byte) 0xC5, (byte) 0xE0, (byte) 0x59, (byte) 0xED, (byte) 0xC5, (byte) 0xD8, (byte) 0x58, (byte) 0xE5,
          (byte) 0xC5, (byte) 0xF8, (byte) 0x59, (byte) 0xEE, (byte) 0xC5, (byte) 0xF0, (byte) 0x59, (byte) 0xF7,
          (byte) 0xC5, (byte) 0xD0, (byte) 0x58, (byte) 0xEE, (byte) 0xC5, (byte) 0xD8, (byte) 0x58, (byte) 0xE5,
          (byte) 0xC5, (byte) 0xF8, (byte) 0x11, (byte) 0x61, (byte) 0x10, (byte) 0xC4, (byte) 0xE2, (byte) 0x79,
          (byte) 0x18, (byte) 0x62, (byte) 0x20, (byte) 0xC4, (byte) 0xE2, (byte) 0x79, (byte) 0x18, (byte) 0x6A,
          (byte) 0x24, (byte) 0xC4, (byte) 0xE2, (byte) 0x79, (byte) 0x18, (byte) 0x72, (byte) 0x28, (byte) 0xC4,
          (byte) 0xE2, (byte) 0x79, (byte) 0x18, (byte) 0x7A, (byte) 0x2C, (byte) 0xC5, (byte) 0xE8, (byte) 0x59,
          (byte) 0xE4, (byte) 0xC5, (byte) 0xE0, (byte) 0x59, (byte) 0xED, (byte) 0xC5, (byte) 0xD8, (byte) 0x58,
          (byte) 0xE5, (byte) 0xC5, (byte) 0xF8, (byte) 0x59, (byte) 0xEE, (byte) 0xC5, (byte) 0xF0, (byte) 0x59,
          (byte) 0xF7, (byte) 0xC5, (byte) 0xD0, (byte) 0x58, (byte) 0xEE, (byte) 0xC5, (byte) 0xD8, (byte) 0x58,
          (byte) 0xE5, (byte) 0xC5, (byte) 0xF8, (byte) 0x11, (byte) 0x61, (byte) 0x20, (byte) 0xC4, (byte) 0xE2,
          (byte) 0x79, (byte) 0x18, (byte) 0x62, (byte) 0x30, (byte) 0xC4, (byte) 0xE2, (byte) 0x79, (byte) 0x18,
          (byte) 0x6A, (byte) 0x34, (byte) 0xC4, (byte) 0xE2, (byte) 0x79, (byte) 0x18, (byte) 0x72, (byte) 0x38,
          (byte) 0xC4, (byte) 0xE2, (byte) 0x79, (byte) 0x18, (byte) 0x7A, (byte) 0x3C, (byte) 0xC5, (byte) 0xE8,
          (byte) 0x59, (byte) 0xE4, (byte) 0xC5, (byte) 0xE0, (byte) 0x59, (byte) 0xED, (byte) 0xC5, (byte) 0xD8,
          (byte) 0x58, (byte) 0xE5, (byte) 0xC5, (byte) 0xF8, (byte) 0x59, (byte) 0xEE, (byte) 0xC5, (byte) 0xF0,
          (byte) 0x59, (byte) 0xF7, (byte) 0xC5, (byte) 0xD0, (byte) 0x58, (byte) 0xEE, (byte) 0xC5, (byte) 0xD8,
          (byte) 0x58, (byte) 0xE5, (byte) 0xC5, (byte) 0xF8, (byte) 0x11, (byte) 0x61, (byte) 0x30, (byte) 0xC4,
          (byte) 0xE2, (byte) 0x79, (byte) 0x18, (byte) 0x62, (byte) 0x40, (byte) 0xC4, (byte) 0xE2, (byte) 0x79,
          (byte) 0x18, (byte) 0x6A, (byte) 0x44, (byte) 0xC4, (byte) 0xE2, (byte) 0x79, (byte) 0x18, (byte) 0x72,
          (byte) 0x48, (byte) 0xC4, (byte) 0xE2, (byte) 0x79, (byte) 0x18, (byte) 0x7A, (byte) 0x4C, (byte) 0xC5,
          (byte) 0xE8, (byte) 0x59, (byte) 0xD4, (byte) 0xC5, (byte) 0xE0, (byte) 0x59, (byte) 0xDD, (byte) 0xC5,
          (byte) 0xE8, (byte) 0x58, (byte) 0xD3, (byte) 0xC5, (byte) 0xF8, (byte) 0x59, (byte) 0xC6, (byte) 0xC5,
          (byte) 0xF0, (byte) 0x59, (byte) 0xCF, (byte) 0xC5, (byte) 0xF8, (byte) 0x58, (byte) 0xC1, (byte) 0xC5,
          (byte) 0xE8, (byte) 0x58, (byte) 0xC0, (byte) 0xC5, (byte) 0xF8, (byte) 0x11, (byte) 0x41, (byte) 0x40,
          (byte) 0xC3};
  private static final byte[] MUL_WINDOWS = {
          (byte) 0xC5, (byte) 0xF8, (byte) 0x10, (byte) 0x52, (byte) 0x10, (byte) 0xC5, (byte) 0xF8, (byte) 0x10,
          (byte) 0x5A, (byte) 0x20, (byte) 0xC5, (byte) 0xF8, (byte) 0x10, (byte) 0x42, (byte) 0x30, (byte) 0xC5,
          (byte) 0xF8, (byte) 0x10, (byte) 0x4A, (byte) 0x40, (byte) 0xC4, (byte) 0xC2, (byte) 0x79, (byte) 0x18,
          (byte) 0x60, (byte) 0x10, (byte) 0xC4, (byte) 0xC2, (byte) 0x79, (byte) 0x18, (byte) 0x68, (byte) 0x14,
          (byte) 0xC4, (byte) 0xC2, (byte) 0x79, (byte) 0x18, (byte) 0x70, (byte) 0x18, (byte) 0xC4, (byte) 0xC2,
          (byte) 0x79, (byte) 0x18, (byte) 0x78, (byte) 0x1C, (byte) 0xC5, (byte) 0xE8, (byte) 0x59, (byte) 0xE4,
          (byte) 0xC5, (byte) 0xE0, (byte) 0x59, (byte) 0xED, (byte) 0xC5, (byte) 0xD8, (byte) 0x58, (byte) 0xE5,
          (byte) 0xC5, (byte) 0xF8, (byte) 0x59, (byte) 0xEE, (byte) 0xC5, (byte) 0xF0, (byte) 0x59, (byte) 0xF7,
          (byte) 0xC5, (byte) 0xD0, (byte) 0x58, (byte) 0xEE, (byte) 0xC5, (byte) 0xD8, (byte) 0x58, (byte) 0xE5,
          (byte) 0xC4, (byte) 0xC1, (byte) 0x78, (byte) 0x11, (byte) 0x61, (byte) 0x10, (byte) 0xC4, (byte) 0xC2,
          (byte) 0x79, (byte) 0x18, (byte) 0x60, (byte) 0x20, (byte) 0xC4, (byte) 0xC2, (byte) 0x79, (byte) 0x18,
          (byte) 0x68, (byte) 0x24, (byte) 0xC4, (byte) 0xC2, (byte) 0x79, (byte) 0x18, (byte) 0x70, (byte) 0x28,
          (byte) 0xC4, (byte) 0xC2, (byte) 0x79, (byte) 0x18, (byte) 0x78, (byte) 0x2C, (byte) 0xC5, (byte) 0xE8,
          (byte) 0x59, (byte) 0xE4, (byte) 0xC5, (byte) 0xE0, (byte) 0x59, (byte) 0xED, (byte) 0xC5, (byte) 0xD8,
          (byte) 0x58, (byte) 0xE5, (byte) 0xC5, (byte) 0xF8, (byte) 0x59, (byte) 0xEE, (byte) 0xC5, (byte) 0xF0,
          (byte) 0x59, (byte) 0xF7, (byte) 0xC5, (byte) 0xD0, (byte) 0x58, (byte) 0xEE, (byte) 0xC5, (byte) 0xD8,
          (byte) 0x58, (byte) 0xE5, (byte) 0xC4, (byte) 0xC1, (byte) 0x78, (byte) 0x11, (byte) 0x61, (byte) 0x20,
          (byte) 0xC4, (byte) 0xC2, (byte) 0x79, (byte) 0x18, (byte) 0x60, (byte) 0x30, (byte) 0xC4, (byte) 0xC2,
          (byte) 0x79, (byte) 0x18, (byte) 0x68, (byte) 0x34, (byte) 0xC4, (byte) 0xC2, (byte) 0x79, (byte) 0x18,
          (byte) 0x70, (byte) 0x38, (byte) 0xC4, (byte) 0xC2, (byte) 0x79, (byte) 0x18, (byte) 0x78, (byte) 0x3C,
          (byte) 0xC5, (byte) 0xE8, (byte) 0x59, (byte) 0xE4, (byte) 0xC5, (byte) 0xE0, (byte) 0x59, (byte) 0xED,
          (byte) 0xC5, (byte) 0xD8, (byte) 0x58, (byte) 0xE5, (byte) 0xC5, (byte) 0xF8, (byte) 0x59, (byte) 0xEE,
          (byte) 0xC5, (byte) 0xF0, (byte) 0x59, (byte) 0xF7, (byte) 0xC5, (byte) 0xD0, (byte) 0x58, (byte) 0xEE,
          (byte) 0xC5, (byte) 0xD8, (byte) 0x58, (byte) 0xE5, (byte) 0xC4, (byte) 0xC1, (byte) 0x78, (byte) 0x11,
          (byte) 0x61, (byte) 0x30, (byte) 0xC4, (byte) 0xC2, (byte) 0x79, (byte) 0x18, (byte) 0x60, (byte) 0x40,
          (byte) 0xC4, (byte) 0xC2, (byte) 0x79, (byte) 0x18, (byte) 0x68, (byte) 0x44, (byte) 0xC4, (byte) 0xC2,
          (byte) 0x79, (byte) 0x18, (byte) 0x70, (byte) 0x48, (byte) 0xC4, (byte) 0xC2, (byte) 0x79, (byte) 0x18,
          (byte) 0x78, (byte) 0x4C, (byte) 0xC5, (byte) 0xE8, (byte) 0x59, (byte) 0xD4, (byte) 0xC5, (byte) 0xE0,
          (byte) 0x59, (byte) 0xDD, (byte) 0xC5, (byte) 0xE8, (byte) 0x58, (byte) 0xD3, (byte) 0xC5, (byte) 0xF8,
          (byte) 0x59, (byte) 0xC6, (byte) 0xC5, (byte) 0xF0, (byte) 0x59, (byte) 0xCF, (byte) 0xC5, (byte) 0xF8,
          (byte) 0x58, (byte) 0xC1, (byte) 0xC5, (byte) 0xE8, (byte) 0x58, (byte) 0xC0, (byte) 0xC4, (byte) 0xC1,
          (byte) 0x78, (byte) 0x11, (byte) 0x41, (byte) 0x40, (byte) 0xC3};

  static {
    try {
      WithJvmci.link();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  public static native void mulAvx(Matrix4f a, Matrix4f b, Matrix4f r);
  public static native void invert(Matrix4f a, Matrix4f r);

  public static void link() throws Exception {
    String os = System.getProperty("os.name");
    boolean isWindows = os.contains("Windows");
    JVMCIBackend jvmci = JVMCI.getRuntime().getHostJVMCIBackend();
    {
      byte[] code = isWindows ? MUL_WINDOWS : MUL_LINUX;
      int length = code.length;
      Method m = WithJvmci.class.getDeclaredMethod("mulAvx", Matrix4f.class, Matrix4f.class, Matrix4f.class);
      ResolvedJavaMethod rm = jvmci.getMetaAccess().lookupJavaMethod(m);
      HotSpotCompiledNmethod nm = new HotSpotCompiledNmethod(m.getName(), code, length, new Site[0],
              new Assumptions.Assumption[0], new ResolvedJavaMethod[0], new HotSpotCompiledCode.Comment[0], new byte[0], 1,
              new DataPatch[0], true, 0, null, (HotSpotResolvedJavaMethod) rm, JVMCICompiler.INVOCATION_ENTRY_BCI, 1, 0, false);
      jvmci.getCodeCache().setDefaultCode(rm, nm);
    }
    {
      byte[] code = isWindows ? INVERT_WINDOWS : INVERT_LINUX;
      int length = code.length;
      Method m = WithJvmci.class.getDeclaredMethod("invert", Matrix4f.class, Matrix4f.class);
      ResolvedJavaMethod rm = jvmci.getMetaAccess().lookupJavaMethod(m);
      HotSpotCompiledNmethod nm = new HotSpotCompiledNmethod(m.getName(), code, length, new Site[0],
              new Assumptions.Assumption[0], new ResolvedJavaMethod[0], new HotSpotCompiledCode.Comment[0], new byte[0], 1,
              new DataPatch[0], true, 0, null, (HotSpotResolvedJavaMethod) rm, JVMCICompiler.INVOCATION_ENTRY_BCI, 1, 0, false);
      jvmci.getCodeCache().setDefaultCode(rm, nm);
    }
  }
}
