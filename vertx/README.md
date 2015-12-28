TransactionalVert.x
===================

Start by adding the contents of lib and etc directories to your classpath. These are here for convenience and you should always take the most recent versions from maven.

Don't forget to install and set up Vert.x!

Next go into each example directory and see the corresponding readme.

Note, this was built against Vert.x 2.x and would definitely need revisiting for later versions of Vert.x. The module directory contains a Vert.x module for STM - will need updating too for more recent versions of Vert.x.

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
