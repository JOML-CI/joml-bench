# How to run

With JAVA_HOME and PATH pointing to a Panama vectorIntrinsics build, run:

```
./mvnw package && java --add-modules jdk.incubator.vector -jar target/bench.jar
```

Without having a local Panama vectorIntrinsics build, run:
```
./ci.sh
```
This will shallow-clone the [GitHub mirror of the Panama vectorIntrinsics branch](https://github.com/openjdk/panama-vector/tree/vectorIntrinsics), build the JDK and execute the benchmarks using it. Make sure your system fulfills the [OpenJDK build requirements](https://github.com/openjdk/panama-vector/blob/vectorIntrinsics/doc/building.md). See the section "Clean Ubuntu Setup" below for a clean Ubuntu setup.
The space requirements for such a cloned and fully built JDK is ~5.6GB, which will reside inside of the panama-vector directory.
In addition, the hsdis utility library is built and installed into the JDK's lib directory.

## Clean Ubuntu Setup (tested on Ubuntu 20.04)

```
sudo apt install -y openjdk-14-jdk-headless \
                    libasound2-dev \
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
                    build-essential
```

## Seeing the disassembly

In order to see the x86 code generated by the JIT compiler for all methods, run:
```
java --add-modules jdk.incubator.vector -XX:+UnlockDiagnosticVMOptions -XX:CompileCommand=print,*Matrix*.* -cp target/bench.jar bench.C2
```
The x86 code is then printed to stdout. This requires the hsdis utility library available in the $JAVA_HOME/lib directory, as is provided by `./ci.sh`.

# Results

## Intel Xeon E-2176M
### With Default Bounds Checks
```
Benchmark                      Mode  Cnt   Score   Error  Units
Bench.Matrix4f_storePutBB      avgt    5   7.938 ± 0.166  ns/op
Bench.Matrix4f_storePutFB      avgt    5   6.243 ± 0.033  ns/op
Bench.Matrix4f_storeU          avgt    5   2.599 ± 0.011  ns/op
Bench.Matrix4fvArr_storePutFB  avgt    5   4.995 ± 0.014  ns/op
Bench.Matrix4fvArr_storeU      avgt    5   2.796 ± 0.007  ns/op
Bench.Matrix4fvArr_storeV256   avgt    5   3.021 ± 0.006  ns/op
Bench.Matrix4fvArr_storeV512   avgt    5  35.075 ± 4.576  ns/op
Bench.mul128LoopArr            avgt    5  31.259 ± 0.797  ns/op
Bench.mul128LoopBB             avgt    5  28.592 ± 0.181  ns/op
Bench.mul128UnrolledArr        avgt    5  30.338 ± 1.197  ns/op
Bench.mul128UnrolledBB         avgt    5  35.368 ± 0.125  ns/op
Bench.mul256Arr                avgt    5  29.858 ± 0.293  ns/op
Bench.mul256BB                 avgt    5  37.207 ± 0.097  ns/op
Bench.mulAffineScalarFma       avgt    5  11.251 ± 0.065  ns/op
Bench.mulJniAVX                avgt    5  13.405 ± 0.049  ns/op
Bench.mulJniSSE                avgt    5  14.204 ± 0.075  ns/op
Bench.mulScalar                avgt    5  19.536 ± 0.984  ns/op
Bench.mulScalarFma             avgt    5  13.224 ± 0.082  ns/op
Bench.noopJni                  avgt    5   9.816 ± 0.045  ns/op
```
### With -Djdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK=0
```
Benchmark                      Mode  Cnt   Score   Error  Units
Bench.Matrix4f_storePutBB      avgt    5   8.409 ± 0.241  ns/op
Bench.Matrix4f_storePutFB      avgt    5   5.325 ± 0.023  ns/op
Bench.Matrix4f_storeU          avgt    5   2.721 ± 0.020  ns/op
Bench.Matrix4fvArr_storePutFB  avgt    5   4.789 ± 0.045  ns/op
Bench.Matrix4fvArr_storeU      avgt    5   2.956 ± 0.036  ns/op
Bench.Matrix4fvArr_storeV256   avgt    5   2.303 ± 0.005  ns/op
Bench.Matrix4fvArr_storeV512   avgt    5  35.158 ± 1.596  ns/op
Bench.mul128LoopArr            avgt    5  21.667 ± 0.109  ns/op
Bench.mul128LoopBB             avgt    5  25.870 ± 0.062  ns/op
Bench.mul128UnrolledArr        avgt    5  25.297 ± 0.342  ns/op
Bench.mul128UnrolledBB         avgt    5  26.480 ± 0.072  ns/op
Bench.mul256Arr                avgt    5  33.100 ± 0.103  ns/op
Bench.mul256BB                 avgt    5  33.490 ± 0.258  ns/op
Bench.mulAffineScalarFma       avgt    5  11.040 ± 0.086  ns/op
Bench.mulJniAVX                avgt    5  13.501 ± 0.060  ns/op
Bench.mulJniSSE                avgt    5  14.186 ± 0.190  ns/op
Bench.mulScalar                avgt    5  17.892 ± 0.021  ns/op
Bench.mulScalarFma             avgt    5  13.396 ± 0.062  ns/op
Bench.noopJni                  avgt    5  10.562 ± 0.075  ns/op
```
### With -Djdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK=0 and AbstractShuffle.checkIndexes_Use_VECTOR_ACCESS_OOB_CHECK.patch
See: https://mail.openjdk.java.net/pipermail/panama-dev/2020-May/009302.html
```
Benchmark                     Mode  Cnt  Score   Error  Units
Bench.Matrix4f_storeU         avgt    5   2.614 ± 0.023  ns/op
Bench.Matrix4fvArr_storeU     avgt    5   2.797 ± 0.020  ns/op
Bench.Matrix4fvArr_storeV256  avgt    5   2.257 ± 0.042  ns/op
Bench.mul128LoopArr           avgt    5   8.584 ± 0.136  ns/op
Bench.mul128LoopBB            avgt    5  18.107 ± 0.148  ns/op
Bench.mul128UnrolledArr       avgt    5   9.158 ± 0.114  ns/op
Bench.mul128UnrolledBB        avgt    5  16.129 ± 0.250  ns/op
Bench.mul256Arr               avgt    5   8.519 ± 0.043  ns/op
Bench.mul256BB                avgt    5  10.390 ± 0.056  ns/op
Bench.mulAffineScalarFma      avgt    5  11.271 ± 0.188  ns/op
Bench.mulJniAVX               avgt    5  13.562 ± 0.044  ns/op
Bench.mulJniSSE               avgt    5  14.509 ± 0.641  ns/op
Bench.mulScalar               avgt    5  19.276 ± 0.755  ns/op
Bench.mulScalarFma            avgt    5  15.644 ± 0.083  ns/op
Bench.noopJni                 avgt    5  10.702 ± 0.028  ns/op
```

## Intel Xeon Platinum 8151
### With -XX:UseAVX=3 and Default Bounds Checks
```
Benchmark                      Mode  Cnt   Score    Error  Units
Bench.Matrix4f_storePutBB      avgt    5   7.638 ±  0.001  ns/op
Bench.Matrix4f_storePutFB      avgt    5   4.844 ±  0.002  ns/op
Bench.Matrix4f_storeU          avgt    5   2.702 ±  0.005  ns/op
Bench.Matrix4fvArr_storePutFB  avgt    5   4.758 ±  0.001  ns/op
Bench.Matrix4fvArr_storeU      avgt    5   2.906 ±  0.005  ns/op
Bench.Matrix4fvArr_storeV256   avgt    5   3.958 ±  0.009  ns/op
Bench.Matrix4fvArr_storeV512   avgt    5   2.505 ±  0.001  ns/op
Bench.mul128LoopArr            avgt    5  27.546 ±  0.009  ns/op
Bench.mul128LoopBB             avgt    5  36.679 ±  0.548  ns/op
Bench.mul128UnrolledArr        avgt    5  25.923 ±  0.226  ns/op
Bench.mul128UnrolledBB         avgt    5  34.298 ±  0.054  ns/op
Bench.mul256Arr                avgt    5  32.896 ±  0.205  ns/op
Bench.mul256BB                 avgt    5  26.297 ±  0.001  ns/op
Bench.mulAffineScalarFma       avgt    5   9.925 ±  0.016  ns/op
Bench.mulJniAVX                avgt    5  12.521 ±  0.001  ns/op
Bench.mulJniSSE                avgt    5  12.271 ±  0.001  ns/op
Bench.mulScalar                avgt    5  18.463 ±  0.007  ns/op
Bench.mulScalarFma             avgt    5  13.836 ±  0.003  ns/op
Bench.noopJni                  avgt    5  10.519 ±  0.001  ns/op
```
### With -Djdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK=0 and AbstractShuffle.checkIndexes_Use_VECTOR_ACCESS_OOB_CHECK.patch
See: https://mail.openjdk.java.net/pipermail/panama-dev/2020-May/009302.html
```
Benchmark                     Mode  Cnt   Score    Error  Units
Bench.Matrix4f_storeU         avgt    5   2.703 ±  0.005  ns/op
Bench.Matrix4fvArr_storeU     avgt    5   2.908 ±  0.005  ns/op
Bench.Matrix4fvArr_storeV256  avgt    5   2.754 ±  0.001  ns/op
Bench.Matrix4fvArr_storeV512  avgt    5  31.817 ±  0.402  ns/op
Bench.mul128LoopArr           avgt    5   7.596 ±  0.009  ns/op
Bench.mul128LoopBB            avgt    5  15.373 ±  0.010  ns/op
Bench.mul128UnrolledArr       avgt    5   7.922 ±  0.006  ns/op
Bench.mul128UnrolledBB        avgt    5  15.043 ±  0.132  ns/op
Bench.mul256Arr               avgt    5   8.075 ±  0.002  ns/op
Bench.mul256BB                avgt    5   9.045 ±  0.014  ns/op
Bench.mulAffineScalarFma      avgt    5  10.562 ±  0.012  ns/op
Bench.mulJniAVX               avgt    5  12.522 ±  0.004  ns/op
Bench.mulJniSSE               avgt    5  12.272 ±  0.001  ns/op
Bench.mulScalar               avgt    5  19.038 ±  0.019  ns/op
Bench.mulScalarFma            avgt    5  14.211 ±  0.006  ns/op
Bench.noopJni                 avgt    5  10.518 ±  0.001  ns/op
```
### With -XX:UseAVX=3 and -Djdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK=0
```
Benchmark                      Mode  Cnt   Score    Error  Units
Bench.Matrix4f_storePutBB      avgt    5   7.639 ±  0.004  ns/op
Bench.Matrix4f_storePutFB      avgt    5   4.832 ±  0.010  ns/op
Bench.Matrix4f_storeU          avgt    5   2.701 ±  0.005  ns/op
Bench.Matrix4fvArr_storePutFB  avgt    5   4.758 ±  0.001  ns/op
Bench.Matrix4fvArr_storeU      avgt    5   3.045 ±  0.005  ns/op
Bench.Matrix4fvArr_storeV256   avgt    5   2.120 ±  0.002  ns/op
Bench.Matrix4fvArr_storeV512   avgt    5   1.670 ±  0.001  ns/op
Bench.mul128LoopArr            avgt    5  19.287 ±  0.001  ns/op
Bench.mul128LoopBB             avgt    5  28.899 ±  0.384  ns/op
Bench.mul128UnrolledArr        avgt    5  19.033 ±  0.003  ns/op
Bench.mul128UnrolledBB         avgt    5  28.942 ±  0.223  ns/op
Bench.mul256Arr                avgt    5  29.369 ±  0.190  ns/op
Bench.mul256BB                 avgt    5  32.708 ±  0.129  ns/op
Bench.mulAffineScalarFma       avgt    5   9.864 ±  0.013  ns/op
Bench.mulJniAVX                avgt    5  12.521 ±  0.001  ns/op
Bench.mulJniSSE                avgt    5  12.271 ±  0.001  ns/op
Bench.mulScalar                avgt    5  18.456 ±  0.003  ns/op
Bench.mulScalarFma             avgt    5  13.836 ±  0.028  ns/op
Bench.noopJni                  avgt    5  10.519 ±  0.001  ns/op
```
### With -XX:UseAVX=3, -Djdk.incubator.vector.VECTOR_ACCESS_OOB_CHECK=0 and AbstractShuffle.checkIndexes_Use_VECTOR_ACCESS_OOB_CHECK.patch
```
Benchmark                      Mode  Cnt   Score    Error  Units
Bench.Matrix4f_storePutBB      avgt    5   7.638 ±  0.001  ns/op
Bench.Matrix4f_storePutFB      avgt    5   4.846 ±  0.003  ns/op
Bench.Matrix4f_storeU          avgt    5   2.710 ±  0.098  ns/op
Bench.Matrix4fvArr_storePutFB  avgt    5   4.758 ±  0.001  ns/op
Bench.Matrix4fvArr_storeU      avgt    5   2.906 ±  0.005  ns/op
Bench.Matrix4fvArr_storeV256   avgt    5   2.106 ±  0.001  ns/op
Bench.Matrix4fvArr_storeV512   avgt    5   1.670 ±  0.001  ns/op
Bench.mul128LoopArr            avgt    5   7.627 ±  0.002  ns/op
Bench.mul128LoopBB             avgt    5  15.602 ±  0.035  ns/op
Bench.mul128UnrolledArr        avgt    5   8.781 ±  0.045  ns/op
Bench.mul128UnrolledBB         avgt    5  15.100 ±  0.128  ns/op
Bench.mul256Arr                avgt    5   8.139 ±  0.001  ns/op
Bench.mul256BB                 avgt    5   9.096 ±  0.001  ns/op
Bench.mulAffineScalarFma       avgt    5   9.865 ±  0.013  ns/op
Bench.mulJniAVX                avgt    5  12.522 ±  0.001  ns/op
Bench.mulJniSSE                avgt    5  12.270 ±  0.001  ns/op
Bench.mulScalar                avgt    5  18.458 ±  0.008  ns/op
Bench.mulScalarFma             avgt    5  13.832 ±  0.003  ns/op
Bench.noopJni                  avgt    5  10.517 ±  0.001  ns/op
```
