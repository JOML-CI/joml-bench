# How to run

With JAVA_HOME and PATH pointing to a Panama vectorIntrinsics build, run:

```
./mvnw package && java -jar target/bench.jar
```

Without having a local Panama vectorIntrinsics build, run:
```
./ci.sh
```
This will shallow-clone the [GitHub mirror of the Panama vectorIntrinsics branch](https://github.com/openjdk/panama-vector/tree/vectorIntrinsics), build the JDK and execute the benchmarks using it. Make sure your system fulfills the [OpenJDK build requirements](https://github.com/openjdk/panama-vector/blob/vectorIntrinsics/doc/building.md). See the section "Clean Ubuntu Setup" below for a clean Ubuntu setup.
The space requirements for such a cloned and fully built JDK is ~5.6GB, which will reside inside of the panama-vector directory.
In addition, the hsdis utility library is built and installed into the JDK's lib directory.

## Clean Ubuntu Setup (tested on Ubuntu 22.10)

```
sudo apt install -y libasound2-dev \
                    libfontconfig1-dev \
                    libcups2-dev \
                    libx11-dev \
                    libxext-dev \
                    libxrender-dev \
                    libxrandr-dev \
                    libxtst-dev \
                    libxt-dev \
                    git \
                    zip \
                    unzip \
                    automake \
                    autoconf \
                    build-essential \
                    openjdk-19-jdk-headless
```

## Seeing the disassembly

In order to see the x86 code generated by the JIT compiler for all methods, run:
```
java --add-modules jdk.incubator.vector -XX:+UnlockDiagnosticVMOptions -XX:CompileCommand=print,*Matrix*.* -cp target/bench.jar bench.C2
```
The x86 code is then printed to stdout. This requires the hsdis utility library available in the $JAVA_HOME/lib directory, as is provided by `./ci.sh`.

# Results

## Ryzen 5950X
### With -Djdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK=0
```
java -version
openjdk version "20-ea" 2023-03-21
OpenJDK Runtime Environment (build 20-ea+20-1466)
OpenJDK 64-Bit Server VM (build 20-ea+20-1466, mixed mode, sharing)
```
Results:
```
Benchmark                                 Mode  Cnt   Score   Error  Units
Bench.invert_Matrix4f                     avgt   10  19,923 ± 0,262  ns/op
Bench.invert_Matrix4f_Jvmci               avgt   10   7,758 ± 0,036  ns/op
Bench.invert_Matrix4fvArr_128             avgt   10  81,036 ± 0,287  ns/op
Bench.mulAffine_Matrix4f_FMA              avgt   10   6,350 ± 0,033  ns/op
Bench.mul_Matrix4f                        avgt   10  10,892 ± 0,068  ns/op
Bench.mul_Matrix4f_FMA                    avgt   10   8,493 ± 0,045  ns/op
Bench.mul_Matrix4f_Jvmci_AVX              avgt   10   3,346 ± 0,017  ns/op
Bench.mul_Matrix4f_Jvmci_AVX2             avgt   10   2,459 ± 0,013  ns/op
Bench.mul_Matrix4fn_AVX                   avgt   10   6,136 ± 0,237  ns/op
Bench.mul_Matrix4fn_SSE                   avgt   10   6,743 ± 0,033  ns/op
Bench.mul_Matrix4fvArr_128_Loop           avgt   10   8,316 ± 0,037  ns/op
Bench.mul_Matrix4fvArr_128_Unrolled       avgt   10   8,380 ± 0,012  ns/op
Bench.mul_Matrix4fvArr_256                avgt   10   9,095 ± 0,113  ns/op
Bench.mul_Matrix4fvBB_128_Loop            avgt   10  16,328 ± 0,260  ns/op
Bench.mul_Matrix4fvBB_128_Unrolled        avgt   10  16,971 ± 0,179  ns/op
Bench.mul_Matrix4fvBB_256                 avgt   10  16,741 ± 0,052  ns/op
Bench.noop_Jvmci_2args                    avgt   10   1,296 ± 0,003  ns/op
Bench.noop_Panama_2args                   avgt   10   3,585 ± 0,032  ns/op
Bench.noop_jni_2args                      avgt   10   4,541 ± 0,023  ns/op
Bench.store_Matrix4f_ByteBuffer_putFloat  avgt   10   4,582 ± 0,085  ns/op
Bench.store_Matrix4f_FloatBuffer_put      avgt   10   3,433 ± 0,019  ns/op
Bench.store_Matrix4f_Jvmci_AVX2           avgt   10   1,374 ± 0,016  ns/op
Bench.store_Matrix4f_Unsafe               avgt   10   1,684 ± 0,044  ns/op
Bench.store_Matrix4fvArr_256              avgt   10   7,663 ± 0,015  ns/op
Bench.store_Matrix4fvArr_512              avgt   10  23,101 ± 0,057  ns/op
Bench.store_Matrix4fvArr_FloatBuffer_put  avgt   10   8,854 ± 0,031  ns/op
Bench.store_Matrix4fvArr_Unsafe           avgt   10   2,058 ± 0,014  ns/op
Bench.transpose_Matrix4f                  avgt   10   2,522 ± 0,004  ns/op
Bench.transpose_Matrix4f_Jvmci            avgt   10   1,598 ± 0,014  ns/op
Bench.transpose_Matrix4fvArr_128          avgt   10  64,862 ± 0,316  ns/op
```
