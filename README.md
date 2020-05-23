# How to run

With JAVA_HOME and PATH pointing to a Panama vectorIntrinsics build, run:

```
./mvnw package && java --add-modules jdk.incubator.vector -jar target/bench.jar
```

# Results

## On an Intel Xeon E-2176M

```
Benchmark             Mode  Cnt   Score   Error  Units
Bench.mul128Loop      avgt    5  31.919 ± 0.128  ns/op
Bench.mul128Unrolled  avgt    5  26.283 ± 0.180  ns/op
Bench.mul256          avgt    5  35.105 ± 0.633  ns/op
Bench.mulScalar       avgt    5  21.234 ± 0.122  ns/op
Bench.mulScalarFma    avgt    5  15.387 ± 0.045  ns/op
```
