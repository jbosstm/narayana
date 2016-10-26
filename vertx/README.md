TransactionalVert.x
===================

The vertx sub-project is a port of Mark Little's TransactionalVert.x repo <sup>[1](#history)</sup>. It shows how to use the narayana STM implementation in a Vert.x 3 environment. Integrating STM into Vert.x enables the sharing of objects between verticles in the same Vert.x instance that a safe with respect to concurrent transactional updates.

Take a look at each the README.md files in each sub-directory to see how to use STM for Vert.x:

# docs

  Contains the STM guide

# module 

  STM packaged as a Vert.x module

# echo

  A server verticle which echos back whatever client verticles send.
  This example requires an install of Vert.x. Refer to the README.md file for instructions.
  The jars needed to run the example need to be downloaded and a maven pom is provided to
  perform the download.

# shared

  An example that shows how to safely share data between verticles. 
  There is a variable called INSTANCE\_CNT in ClientVerticle.java that you can change to introduce
  greater concurrency in order to highlight lock contention.

# raw 

  This is not a Vert.x example, rather it is a demonstrator of the locking issues that can occur when usint TxOJ (the api on which STM is built).
  NOTE: This does not belon here and will be removed in a subsequent commit.
  NOTE: This example runs without any issues (ie it does not show any lock contention issues) so I need
        to investigate why that is.

TODO: We have started to look at some async API changes within Narayana (https://github.com/jbosstm/narayana/blob/master/STM/src/main/java/org/jboss/stm/async/Transaction.java) to better conform to the asynchronous model demanded by Vert.x



<a name="githistory">1</a>: To see a history of the commits Mark made to the original rep use the --follow option: git log --follow vertx
