TransactionalVert.x
===================

Simple example of STM and Vert.x integration

In this example we firs start by the shared data concept within Vert.x to share the id of a transactional
object between Verticles (ClientVerticle and SampleVerticle1 initially). The STM instance is defined in
Sample.java interface and SampleLockable class respectively.

First build a fat jar that contains all the classes need to run the example in a single jar:

  mvn clean package

followed by
  PROJECT_VERSION=<current version in pom> such as 5.4.1
  java -jar target/stm-vertex-shared-${PROJECT_VERSION}.Final-SNAPSHOT-fat.jar -Dcom.arjuna.ats.arjuna.common.propertiesFile=jbossts-properties.xml

You should see output similar to:

> Object name: 0:ffffc0a80011:d7f1:536e7c9b:0
> ARJUNA012163: Starting service com.arjuna.ats.arjuna.recovery.ActionStatusService on port 55282 
> ARJUNA012337: TransactionStatusManagerItem host: 127.0.0.1 port: 55282 
> ARJUNA012170: TransactionStatusManager started on port 55282 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService 
> State value is: 11
> State value is: 12
> State value is: 13
> State value is: 14
> State value is: 15
> State value is: 16
> State value is: 17
> State value is: 18
> State value is: 19
> State value is: 20
> ClientVerticle initialised state: 20
> Succeeded in deploying verticle 
> SampleVerticle1 initialised state with: 21
> SampleVerticle1 SUCCEEDED!

Here we have the two verticles sharing the same transactional object (state).

To add more concurency change the value of INSTANCE_CNT in ClientVerticle.java from 0 to 4 and rerun the example fat jar. Depending upon how each new verticle is executed in relation to
the others, you could see:

> Object name: 0:ffffc0a80011:d7e1:536e7ac5:0
> ARJUNA012163: Starting service com.arjuna.ats.arjuna.recovery.ActionStatusService on port 55266 
> ARJUNA012337: TransactionStatusManagerItem host: 127.0.0.1 port: 55266 
> ARJUNA012170: TransactionStatusManager started on port 55266 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService 
> State value is: 11
> State value is: 12
> State value is: 13
> State value is: 14
> State value is: 15
> State value is: 16
> State value is: 17
> SampleVerticle2 initialised state with: 18
> SampleVerticle2 SUCCEEDED!
> com.arjuna.ats.txoj.exceptions.LockStoreException: Persistent store error. 
> com.arjuna.ats.txoj.exceptions.LockStoreException: Persistent store error. 
> ARJUNA015033: LockManager::setlock() cannot load existing lock states 
> com.arjuna.ats.txoj.exceptions.LockStoreException: Persistent store error. 
> ARJUNA015033: LockManager::setlock() cannot load existing lock states 
> com.arjuna.ats.txoj.exceptions.LockStoreException: Persistent store error. 
> ARJUNA015033: LockManager::setlock() cannot load existing lock states 
> SampleVerticle2 initialised state with: 20
> SampleVerticle2 SUCCEEDED!
> SampleVerticle1 initialised state with: 21
> SampleVerticle1 SUCCEEDED!
> SampleVerticle2 initialised state with: 23
> SampleVerticle2 SUCCEEDED!
> SampleVerticle2 initialised state with: 25
> SampleVerticle2 SUCCEEDED!

Or you could see output containing one or more of the following:

> org.jboss.stm.LockException: Thread[vert.x-eventloop-thread-3,5,main] could not set LockMode.WRITE lock. Got: LockResult.REFUSED
> 	at org.jboss.stm.internal.reflect.InvocationHandler.invoke(InvocationHandler.java:344)
> 	at $Proxy13.increment(Unknown Source)
> 	at SampleVerticle1.start(SampleVerticle1.java:43)
> 	at org.vertx.java.platform.Verticle.start(Verticle.java:82)
> 	at org.vertx.java.platform.impl.DefaultPlatformManager$19.run(DefaultPlatformManager.java:1550)
> 	at org.vertx.java.core.impl.DefaultContext$3.run(DefaultContext.java:176)
> 	at io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:354)
> 	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:353)
> 	at io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:101)
> 	at java.lang.Thread.run(Thread.java:722)
> 
> ARJUNA015027: LockManager::unloadState() failed to remove empty lock state for object 0:ffffc0a80011:d7e7:536e7b4c:0 of type /StateManager/LockManager/SampleLockable 
> ARJUNA015037: Lockmanager::releaselock() could not unload new lock states 
> ARJUNA015037: Lockmanager::releaselock() could not unload new lock states 
> ARJUNA012267: ShadowingStore.remove_state() - fd error for 0:ffffc0a80011:d7e7:536e7b4c:0 
> ARJUNA012267: ShadowingStore.remove_state() - fd error for 0:ffffc0a80011:d7e7:536e7b4c:0 
> ARJUNA015027: LockManager::unloadState() failed to remove empty lock state for object 0:ffffc0a80011:d7e7:536e7b4c:0 of type /StateManager/LockManager/SampleLockable 
> 
> ARJUNA012273: ShadowingStore::write_state() - openAndLock failed for /Users/marklittle/github/TransactionalVert.x/shared/ObjectStore/ShadowingStore//StateManager/LockManager/SampleLockable/LockStore/StateManager/LockManager/SampleLockable/0_ffffc0a80011_d7e7_536e7b4c_0 
> ARJUNA012267: ShadowingStore.remove_state() - fd error for 0:ffffc0a80011:d7e7:536e7b4c:0 
> ARJUNA012273: ShadowingStore::write_state() - openAndLock failed for /Users/marklittle/github/TransactionalVert.x/shared/ObjectStore/ShadowingStore//StateManager/LockManager/SampleLockable/LockStore/StateManager/LockManager/SampleLockable/0_ffffc0a80011_d7e7_536e7b4c_0 
> ARJUNA015029: LockManager::unloadState() failed to write new state for object 0:ffffc0a80011:d7e7:536e7b4c:0 of type /StateManager/LockManager/SampleLockable 
> ARJUNA015029: LockManager::unloadState() failed to write new state for object 0:ffffc0a80011:d7e7:536e7b4c:0 of type /StateManager/LockManager/SampleLockable 
> ARJUNA012091: Top-level abort of action 0:ffffc0a80011:d7e7:536e7b4c:2d received TwoPhaseOutcome.FINISH_ERROR from com.arjuna.ats.internal.arjuna.abstractrecords.RecoveryRecord 
> ARJUNA015027: LockManager::unloadState() failed to remove empty lock state for object 0:ffffc0a80011:d7e7:536e7b4c:0 of type /StateManager/LockManager/SampleLockable 
> 
> SampleVerticle2 FAILED!
> ARJUNA015037: Lockmanager::releaselock() could not unload new lock states 
> ARJUNA015035: LockManager::setlock() cannot save new lock states 
> org.jboss.stm.LockException: Thread[vert.x-eventloop-thread-4,5,main] could not set LockMode.READ lock. Got: LockResult.REFUSED
> 	at org.jboss.stm.internal.reflect.InvocationHandler.invoke(InvocationHandler.java:344)
> 	at $Proxy14.value(Unknown Source)
> 	at SampleVerticle2.start(SampleVerticle2.java:41)
> 	at org.vertx.java.platform.Verticle.start(Verticle.java:82)
> 	at org.vertx.java.platform.impl.DefaultPlatformManager$19.run(DefaultPlatformManager.java:1550)
> 	at org.vertx.java.core.impl.DefaultContext$3.run(DefaultContext.java:176)
> 	at io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:354)
> 	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:353)
> 	at io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:101)
> 	at java.lang.Thread.run(Thread.java:722)

The multiple verticle instances are likely to interfere with each other as they try to read or write to the
same object. The transaction system will always ensure consistency and in the case of a conflict or error such as
those mentioned above, the transaction will be rolled back. A retry attempt will eventually succeed. Our example does
not do retries as it is simply meant as informative.

Note, several of these warning messages could be info/debug and we are considering changes. See https://issues.jboss.org/browse/JBTM-2168
for further details.
