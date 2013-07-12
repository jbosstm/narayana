
A project that shows how to perform transactional work in a RESTful environment.
It is based on draft 4 of a specification available from
http://www.jboss.org/reststar/specifications/transactions.html
with some modifications listed in ./docs/REST-Atomic+v2+draft+4.txt
(changes are marked with lines that begin with the text =>)

The project contains two (maven) modules:

 - tx contains the implementation (the JAX-RS resources that implement the coordinator)
 - webservice (packages the tx module artifact into a web archive for deployment to a web container)

The unit tests use Jersey as the JAX-RS implementation.
The integration tests use Jersey for the particpant and Resteasy for the coordinator.
Jersey and Resteasy are JAX-RS (JSR311) implementations.

There is a utility class called org.jboss.jbossts.star.util.TxSupport to help clients and participants
conform to this specification. You may like to use it directly or else just read the code to get a feel
for how to conform using your own code (perhaps you are using a different language or want to use a faster
HTTP library instead of the default java.util.net.HttpURLConnection).

To use the implementation you will need an initial URL for starting transactions. The default is
TxSupport.TXN_MGR_URL and uses localhost on port 8080. To change it (and still use the TxSupport utility)
call TxSupport.setTxnMgrUrl(...).

Building
========
To build the coordinator jar and war

	mvn clean install

Module tx contains unit tests that show how to implement the client and transactional participants.
The coordinator and participant both run in a single embedded container (https://grizzly-servlet-container.dev.java.net/).

To run the integration tests you will need to have a running instance of the JBoss application server
(http://www.jboss.org/jbossas/). Start the with the default server and set the JBOSS_HOME environment variable
appropriately. I have tested against AS trunk (6.0.0.20100721-M4) and AS7.
The integration tests run the particpant in an embedded container and the coordinator in the JBoss AS:

	mvn clean install -Premote

Generating a Schema
===================

Parts of the API use media type "application/txstatusext+xml". The JAXB classes supporting this media
type are in the restat-util module. To generate a schema for this type compile restat-util and then use
the JAXB shemagen tool:

	schemagen util/src/main/java/org/jboss/jbossts/star/util/media/txstatusext/TransactionManagerElement.java -cp util/target/classes/

which will generate a file called schema1.xsd in the current directory. Then overwrite the existing schema:

	mv schema1.xsd util/src/main/resources/restat.xsd

Next you need to generate an instance of JAXB ObjectFactory using the xjc tool:

	xjc -p org.jboss.jbossts.star.util.media.txstatusext util/src/main/resources/restat.xsd

and copy the ObjectFactory to the package where the other JAXB classes are:

	cp org/jboss/jbossts/star/util/media/txstatusext/ObjectFactory.java  util/src/main/java/org/jboss/jbossts/star/util/media/txstatusext/

and then clean up the generated files:

	rm -rf org
