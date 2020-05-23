# Object Field Coverage

Bachelor Thesis implementation of a new code quality metric built with [Spoon](https://github.com/INRIA/spoon). 
Currently in development 💻

## Build Requirements

### Building spoon 

To build this project, you need to build [this](https://github.com/dnl50/spoon/tree/spoon-core-8.1.0-equals-fix)
fork of spoon and copy the jar to `libs/spoon-core-8.1.1-SNAPSHOT.jar` in the root directory of the project. This is 
necessary because of the problems mentioned in [Spoon Issue 3365](https://github.com/INRIA/spoon/issues/3365). 
The `mavenLocal()` repository offered by gradle does not work unfortunately.

This command can be used to build the required jar file. 

`mvn clean package -Djar.finalName=spoon-core-8.1.1-SNAPSHOT -DskipTests -P release` 

It is recommended to use Java 8 for building spoon, since Java 9+ result in JavaDoc errors which can only be ignored
by editing the pom file.

## Disclaimer

_This README is a work in progress 🤖_
