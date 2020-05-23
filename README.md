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
Benchmark             Mode  Cnt   Score   Error  Units
Bench.mul128Loop      avgt    5  40.239 ± 0.397  ns/op
Bench.mul128Unrolled  avgt    5  35.796 ± 0.658  ns/op
Bench.mul256          avgt    5  41.124 ± 0.152  ns/op
Bench.mulJniSSE       avgt    5  16.960 ± 0.064  ns/op
Bench.mulScalar       avgt    5  21.271 ± 0.142  ns/op
Bench.mulScalarFma    avgt    5  15.630 ± 0.103  ns/op
```

