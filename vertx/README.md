TransactionalVert.x
===================

Example of STM and Vert.x integration

This is a WIP and packaging is likely to change. But for now, here's a sample of how you could use
the Narayana STM implementation within Vert.x

It's based on the already shipping Echo example in Vert.x. However, in this case rather than just
sending a stream of numbers between client and server, we maintain the existing number within a
transactional object. This object can be shared between multiple instances of the EchoClient and the
tranasctions will be serialised correctly.

All of the transactional additions are in the EchoClient.java

If you are running this for the first time then leave as is.
If you are running this more than once and want clients to share the STM objects between
address spaces then go into the ObjectStore dir and look for the Uid that represents the state
you want to share. Then uncomment the Uid line  and replace the Uid in quotes with the Uid
you have selected. Uncomment the other obj1 creation line and comment out the original.

If you want to see how this might work then just go with the example state in the ObjectStore
shipped as part of this example and uncomment the lines.
