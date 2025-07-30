[![Narayana](https://design.jboss.org/narayana/logo/final/narayana_logo_600px.png)](https://narayana.io/)

[![Version](https://img.shields.io/maven-central/v/org.jboss.narayana/narayana-all?logo=apache-maven&style=for-the-badge)](https://search.maven.org/artifact/org.jboss.narayana/narayana-all)

# Narayana

Website: https://narayana.io

Twitter: https://twitter.com/narayana_io, using twitter handle [#narayanaio](https://twitter.com/search?q=%23narayanaio)

## Getting help

If you need help with using Narayana, please visit our forums at:
https://groups.google.com/g/narayana-users
or ask a question on our zulip channel (https://narayana.zulipchat.com/#).

If you would like to contribute a pull request to help the project out the file [CONTRIBUTING.md](https://github.com/jbosstm/narayana/blob/main/CONTRIBUTING.md) contains some guidance on how to do so.

If you have a performance optimization that you would like to suggest to us, please read our document over here:
https://developer.jboss.org/wiki/PerformanceGatesForAcceptingPerformanceFixesInNarayana

### Support Guarantees

Although there are no formal guarantees to support this software the team and, we hope, the broader community will make every effort to resolve queries, fix bugs and add agreed upon features as quickly as resources allow. Community fixes and enhancements can only be expected to be considered for merging onto the main branch.

## Requirements

To build this project you will need a JDK (Java Development Kit) with a minimum version of 17 and a Maven with a minimum version specified in the [maven wrapper properties file](.mvn/wrapper/maven-wrapper.properties)

The commands to do this will vary depending upon which operating system you are building on.

## Building Narayana

To build Narayana you should call:

	./build.[sh|bat] <maven_goals, default is install>
	
To build Narayana without running tests you can call:

	./build.[sh|bat] -DskipTests


If you are building the "community" profile and are using a different maven installation to the one provided by maven wrapper `./mvn` you need to make sure you have the following options:

	-Dorson.jar.location=/full/path/to/checkout/location/ext/
	
The uber jar for the JDK ORB is available here:

	./ArjunaJTS/narayana-jts-idlj/target/narayana-jts-idlj-<VERSION>.jar

The user jar for local JTA is here:

	./ArjunaJTA/narayana-jta/target/narayana-jta-<VERSION>.jar

If you just need the facilities provided by ArjunaCore:

	./ArjunaCore/arjunacore/target/arjunacore-<VERSION>.jar
	
## Building specific components

If you would like to build an individual module (say arjuna) with its dependencies you would type:

	./build.[sh|bat] [clean] install -pl :arjuna -am
	
Other interesting specific components can be built using:

ArjunaCore: `./build.[sh|bat] -am -pl :arjunacore`

NarayanaJTA: `./build.[sh|bat] -am -pl :narayana-jta`

NarayanaJTS (idlj): `./build.[sh|bat] -am -pl :narayana-jts-idlj`

XTS: `./build.[sh|bat] -am -pl :jboss-xts`

STM: `./build.[sh|bat] -am -pl :stm`

## Narayana (BOM) Bill Of Materials

maven BOM dependency used to encapsulate all the dependencies required by Narayana.

    <dependencyManagement>
     <dependencies>
       <dependency>
         <groupId>org.jboss.narayana</groupId>
         <artifactId>narayana-bom</artifactId>
         <version>6.0.1.Final-SNAPSHOT</version>
         <type>pom</type>
         <scope>import</scope>
       </dependency>
     </dependencies>
   </dependencyManagement>

## Testing Narayana

There are three types of tests in the Narayana repository.

* Unit tests which are run with maven surefire and they do not need any special setup.
  Unit tests are run automatically when Narayana is build and if it's not specified otherwise (e.g. with maven flag `-DskipTests`)
* Integration tests are run with maven surefire or maven failsafe. They are run with use of the Arquillian
  and you need to explicitly enable them by activating profile `arq` (maven flag `-Parq`).
  There is a difficulty that each module have different requirements for the integration tests to be run.
  Most of them requires environmental variable `JBOSS_HOME` to be defined and points to an existing
  directory of [WildFly](https://wildfly.org/downloads/). But some of them requires additional steps
  for WildFly being configured. The best way to find out details is to check the [narayana.sh script](scripts/hudson/narayana.sh)
  which is used to run CI tests.
* Integration qa suite resides in the directory `qa/` and contains form of integration tests.
  These are built but not run automatically. See [qa/README.txt](qa/README.txt) for usage. In brevity launching tests
  is about running commands:

      cd qa/
      ant -f run-tests.xml ci-tests


## Code Coverage Testing

      ./build.[sh|bat] -PcodeCoverage (the output is in ${project.build.directory}/coverage.html)

## Checkstyle

Narayana expect usage of the style of code defined by WildFly checkstyle (maven artifact 
[org.wildfly.checkstyle:wildfly-checkstyle-config](https://github.com/wildfly/wildfly-checkstyle-config)).

Because of historical reasons the checkstyle is applied only at newly developed Narayana modules.
The old ones are left without strict code style rules. There is only a recommendation to follow
code style used in the particular file you edit.

Checkstyle checking is bound to maven install phase and if the file does not comply with the defined rules
the compilation fails.

To get your developer life easier use the checkstyle plugins for your IDE

* clone the repo with the
  [checkstyle.xml](https://github.com/wildfly/wildfly-checkstyle-config/blob/master/src/main/resources/wildfly-checkstyle/checkstyle.xml)
  file https://github.com/wildfly/wildfly-checkstyle-config
* install checkstyle plugin to your favourite IDE
    - IntelliJ IDEA: https://plugins.jetbrains.com/plugin/1065-checkstyle-idea
    - Eclipse: https://checkstyle.org/eclipse-cs/#!/
* configure plugin to consume the *checkstyle.xml* and being applied to the particular module

The WildFly provides a formatter complying with the checkstyle rules. If interested check the IDE configs
at project [wildfly-core](https://github.com/wildfly/wildfly-core/tree/main/ide-configs).

## Now The Gory Details.

Each module contains a set of maven build scripts, which chiefly just inherits and selectively overrides the parent
 pom.xml  Understanding this approach requires some knowledge of maven's inheritance.

Top level maven builds always start from scratch. Individual module builds on the other hand are incremental,
such that you may rebuild a single module by traversing into its directory and running 'mvn', but only if you
have first built any pre-req modules e.g. via a parent build.

In addition to driving the build of individual modules, the build files in the bundles directories (ArjunaCore,
ArjunaJTA, ArjunaJTS) contain steps to assemble the release directory structure, including docs, scripts,
config files and other ancillaries. These call each other in some cases, as JTS is largely a superset of
JTA and JTA in turn a superset of Core.

3rd party dependency management is done via maven. Note that versions of most 3rd party components are resolved via the WildFly component-matrix
pom.xml, even when building standalone releases. The version of WildFly to use is determined by the top level pom.xml

Maven is provided via [maven wrapper](https://github.com/takari/maven-wrapper) with command `./mvnw`.
