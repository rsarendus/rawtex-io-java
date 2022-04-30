# RAWTEX IO for Java

A simple Java library for handling [RAWTEX](https://github.com/rsarendus/rawtex-specification) binary format.


## Build

Building the project requires at least **JDK 8** and [Apache Maven](https://maven.apache.org/).

1. Fetch the project from GitHub:
   <br>`git clone https://github.com/rsarendus/rawtex-io-java.git`

2. Navigate into the project's root directory:
   <br>`cd rawtex-io-java`

3. Build the project using Maven:
   * Compile and package as JARs into the project's `target` directories:
   <br>`mvn clean package`
   * Or compile, package and install into your local repository:
   <br>`mvn clean install`
   * Or for more options read about [Maven Build Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)

By default, JARs and source JARs are packaged for each sub-module.

### JavaDoc

In order to also package JavaDoc JARs, the project has to be built using the `generate-javadocs` profile:

```shell
mvn clean install -Pgenerate-javadocs
```

Generating aggregated JavaDoc for the entire project could be done with the following command:

```shell
mvn javadoc:aggregate
```
