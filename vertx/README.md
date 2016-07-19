TransactionalVert.x
===================

Start by adding the contents of lib and etc directories to your classpath. These are here for convenience and you should always take the most recent versions from maven.

Don't forget to install and set up Vert.x!

It's based on the already shipping Echo example in Vert.x. However, in this case rather than just
sending a stream of numbers between client and server, we maintain the existing number within a
transactional object. This object can be shared between multiple instances of the EchoClient and the
tranasctions will be serialised correctly.

All of the transactional additions are in the EchoClient.java

If you are running this for the first time then leave as is.

If you are running this more than once and want clients to share the STM objects between
address spaces then go into the ObjectStore dir and look for the Uid that represents the state
you want to share. Then uncomment the Uid line  and replace the Uid in quotes with the Uid
you have selected. Uncomment the other obj1 creation line and comment out the original. Then run as
many EchoClients as you want. Depending upon timing and the number of clients, you will likely
see warnings from the system that the state could not be read or written. But consistency remains.

If you want to see how this might work then just go with the example state in the ObjectStore
shipped as part of this example and uncomment the lines.
=======
Next go into each example directory and see the corresponding readme.

Note, this was built against Vert.x 2.x and would definitely need revisiting for later versions of Vert.x. The module directory contains a Vert.x module for STM - will need updating too for more recent versions of Vert.x.

Also started to look at some async API changes within Narayana (https://github.com/jbosstm/narayana/blob/master/STM/src/main/java/org/jboss/stm/async/Transaction.java)

--

Maven artifacts equivalent to lib ...

      <dependency>
      <groupId>org.jboss.narayana.arjunacore</groupId>
      <artifactId>arjunacore</artifactId>
      <version>5.0.1.Final</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>org.jboss.narayana.arjunacore</groupId>
      <artifactId>txoj</artifactId>
      <version>5.0.1.Final</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>org.jboss.logging</groupId>
      <artifactId>jboss-logging</artifactId>
      <version>3.1.4.GA</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>org.jboss.narayana.stm</groupId>
      <artifactId>stm</artifactId>
      <version>5.0.1.Final</version>
      <scope>compile</scope>
    </dependency>
