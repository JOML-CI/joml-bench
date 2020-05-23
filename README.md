# How to run

With JAVA_HOME and PATH pointing to a Panama vectorIntrinsics build, run:

```
./mvnw package && java --add-modules jdk.incubator.vector -jar target/bench.jar
```

Without having a local Panama vectorIntrinsics build, run:
```
./ci.sh
```
This will shallow-clone the [GitHub mirror of the Panama vectorIntrinsics branch](https://github.com/openjdk/panama-vector/tree/vectorIntrinsics), build the JDK and execute the benchmarks using it. Make sure your system fulfills the [OpenJDK build requirements](https://github.com/openjdk/panama-vector/blob/vectorIntrinsics/doc/building.md).
The space requirements for such a cloned and fully built JDK is ~5.6GB, which will reside inside of the panama-vector directory.

# Results

## On an Intel Xeon E-2176M

```
Benchmark                Mode  Cnt   Score   Error  Units
Bench.mul128LoopArr      avgt    5  23.933 ± 0.195  ns/op
Bench.mul128LoopBB       avgt    5  37.562 ± 0.462  ns/op
Bench.mul128UnrolledArr  avgt    5  28.228 ± 0.060  ns/op
Bench.mul128UnrolledBB   avgt    5  40.108 ± 0.401  ns/op
Bench.mul256Arr          avgt    5  34.685 ± 0.374  ns/op
Bench.mul256BB           avgt    5  40.312 ± 0.462  ns/op
Bench.mulJniAVX          avgt    5  12.623 ± 0.379  ns/op
Bench.mulJniSSE          avgt    5  13.888 ± 0.475  ns/op
Bench.mulScalar          avgt    5  22.563 ± 0.088  ns/op
Bench.mulScalarFma       avgt    5  17.296 ± 0.215  ns/op
```
