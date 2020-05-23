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
Bench.mul128LoopArr      avgt    5  33.034 ± 0.370  ns/op
Bench.mul128LoopBB       avgt    5  42.357 ± 0.440  ns/op
Bench.mul128UnrolledArr  avgt    5  32.213 ± 0.476  ns/op
Bench.mul128UnrolledBB   avgt    5  31.871 ± 0.155  ns/op
Bench.mul256Arr          avgt    5  32.419 ± 0.107  ns/op
Bench.mul256BB           avgt    5  29.098 ± 0.230  ns/op
Bench.mulJniSSE          avgt    5  15.233 ± 0.168  ns/op
Bench.mulScalar          avgt    5  20.596 ± 0.047  ns/op
Bench.mulScalarFma       avgt    5  15.263 ± 0.171  ns/op
```
