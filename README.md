# imagej-common-benchmarks

[JMH](http://openjdk.java.net/projects/code-tools/jmh/)-based benchmarks for [ImageJ Common](https://github.com/imagej/imagej-common).

## Execute

1. Install [`table-io`](https://github.com/imagej/imagej-common/tree/table-io) branch of `imagej-common`
2. Build with Maven: `mvn clean package -o -U -Denforcer.skip=true`
3. `java -jar targets/benchmarks.jar`
