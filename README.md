# imagej-benchmarks

[JMH](http://openjdk.java.net/projects/code-tools/jmh/)-based benchmarks for [ImageJ](https://github.com/imagej/) projects.

## Execute

1. Build with Maven: `mvn clean package -o -U -Denforcer.skip=true`
2. `java -jar targets/benchmarks.jar`
