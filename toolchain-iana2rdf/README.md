<img
  src="https://www.iana.org/_img/2022/iana-logo-header.svg"
  width=350px
  alt="iana2rdf"
  title="iana2rdf"
  style="display: inline-block; margin: 0 auto; max-width: 150px">
# iana2rdf

## Methodology
1. Convert source IANA XML to JSON
2. Create JSON-LD context using desired primary name space.
3. SPARQL Update until desired results are achieved.
4. Season to taste.

## Building iana2rdf jar version

1. Must have working JDK21 environment
2. `mvn -Pnative clean native:compile`
3. A runnable jar version "iana2rdf-1.0.0.jar" will be in the target folder

`java -jar iana2rdf-1.0.0.jar -help` will display instructions.

## Building platform specific stand-alone

1. Must have at least JDK21 GraalVM installed with fully functional [native-image](https://www.graalvm.org/latest/reference-manual/native-image/) build enviroment for the platform you are building for.
2. mvn -Pnative clean package
3. Artifact "iana2rdf" will be in target folder.
