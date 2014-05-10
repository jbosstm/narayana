TransactionalVert.x
===================

Simple example of STM and Vert.x integration

It's based on the already shipping Echo example in Vert.x. However, in this case rather than just
sending a stream of numbers between client and server, we maintain the existing number within a
transactional object. This object can be shared between multiple instances of the EchoClient and the
tranasctions will be serialised correctly.

All of the transactional additions are in the EchoClient.java so go take a look later. For now, follow
these steps.

First start the EchoServer using:

vertx run EchoServer.java

Then the EchoClient using:

vertx run EchoClient.java

You should see the following output from the server:

Succeeded in deploying verticle

But the real action is at the client, where you should see something like:

Object name: 0:ffffc0a8000f:e84e:5325d6d2:0
ARJUNA012163: Starting service com.arjuna.ats.arjuna.recovery.ActionStatusService on port 59471 
ARJUNA012337: TransactionStatusManagerItem host: 127.0.0.1 port: 59471 
ARJUNA012170: TransactionStatusManager started on port 59471 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService 
Net client sending: hello11
Net client sending: hello12
Net client sending: hello13
Net client sending: hello14
Net client sending: hello15
Net client sending: hello16
Net client sending: hello17
Net client sending: hello18
Net client sending: hello19
Net client sending: hello20
Net client receiving: hello11
hello12
hello13
hello14
hello15
hello16
hello17
hello18
hello19
hello20

Some of these values will change - specifically the object name identifier (Uid) since that is assigned by the system based
upon information from the running machine.

What we have here is a transactional integer whose value is being used to modify the string exchanged with the server, i.e., hello<integer value>
Each time a string is sent, the integer is incremented within the scope of a transaction so that it is isolated and atomic. In this example, the state
is also durable (saved/restored to/from the local file system). You can see the system state store (log) by looking within the ObjectStore
directory that's just been created in the cwd.

Now let's see what happens if we have multiple clients running at the same time. Let's tqke the object name (Uid) output in the
example (0:ffffc0a8000f:e84e:5325d6d2:0 in this case) and put it into the EchoClient code so that we can run multiple instances of the
client against the same server. Look for the following code in EchoClient.java

       	  // Modify this line if sharing state and uncomment.
	  // Uid u = new Uid("0:ffffc0a80003:c915:529f59de:1");

	  Container<Sample> theContainer = new Container<Sample>("Demo", Container.TYPE.PERSISTENT, Container.MODEL.SHARED);

	  // Modify this line if sharing state and uncomment.
	// Sample obj1 = theContainer.clone(new SampleLockable(10), u);

	  // Comment it out if you are going to share state.
	Sample obj1 = theContainer.create(new SampleLockable(10));

And add the Uid, uncommenting the lines as directed and commenting the other ones. Remember, this is just an example so there will be better
ways to do this in reality! You should therefore end up with something like:

     	  // Modify this line if sharing state and uncomment.
	  // Uid u = new Uid("0:ffffc0a80003:c915:529f59de:1");
	  Uid u = new Uid("0:ffffc0a8000f:e84e:5325d6d2:0");

	  Container<Sample> theContainer = new Container<Sample>("Demo", Container.TYPE.PERSISTENT, Container.MODEL.SHARED);

	  // Modify this line if sharing state and uncomment.
	  Sample obj1 = theContainer.clone(new SampleLockable(10), u);

	  // Comment it out if you are going to share state.
	  // Sample obj1 = theContainer.create(new SampleLockable(10));

Run the server in one shell and two instances of the client, each in their own shell. You should see the same output from the server as previously,
but the client output should be something like:

Succeeded in deploying verticle 
Object name: 0:ffffc0a8000f:e84e:5325d6d2:0
ARJUNA012163: Starting service com.arjuna.ats.arjuna.recovery.ActionStatusService on port 59575 
ARJUNA012337: TransactionStatusManagerItem host: 127.0.0.1 port: 59575 
ARJUNA012170: TransactionStatusManager started on port 59575 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService 
Net client sending: hello21
Net client sending: hello22
Net client sending: hello23
Net client sending: hello24
Net client sending: hello25
Net client sending: hello26
Net client sending: hello27
Net client sending: hello28
Net client sending: hello29
Net client sending: hello30
Net client receiving: hello21
hello22
hello23
hello24
hello25
hello26
hello27
hello28
hello29
hello30

and

Succeeded in deploying verticle 
Object name: 0:ffffc0a8000f:e84e:5325d6d2:0
ARJUNA012163: Starting service com.arjuna.ats.arjuna.recovery.ActionStatusService on port 59578 
ARJUNA012337: TransactionStatusManagerItem host: 127.0.0.1 port: 59578 
ARJUNA012170: TransactionStatusManager started on port 59578 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService 
Net client sending: hello31
Net client sending: hello32
Net client sending: hello33
Net client sending: hello34
Net client sending: hello35
Net client sending: hello36
Net client sending: hello37
Net client sending: hello38
Net client sending: hello39
Net client sending: hello40
Net client receiving: hello31
hello32
hello33
hello34
hello35
hello36
hello37
hello38
hello39
hello40

As you can see, we are sharing the same transational integer and each client manages to increment it in isolation from the other. However, if
you run two clients in the same shell (e.g., using the & to background the first in Unix) like vertx run EchoClient.java & vertx run EchoClient.java
you should see something like:

Succeeded in deploying verticle 
Succeeded in deploying verticle 
Object name: 0:ffffc0a8000f:e84e:5325d6d2:0
Object name: 0:ffffc0a8000f:e84e:5325d6d2:0
ARJUNA012163: Starting service com.arjuna.ats.arjuna.recovery.ActionStatusService on port 59597 
ARJUNA012163: Starting service com.arjuna.ats.arjuna.recovery.ActionStatusService on port 59598 
ARJUNA012337: TransactionStatusManagerItem host: 127.0.0.1 port: 59597 
ARJUNA012337: TransactionStatusManagerItem host: 127.0.0.1 port: 59598 
ARJUNA012170: TransactionStatusManager started on port 59597 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService 
ARJUNA012170: TransactionStatusManager started on port 59598 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService 
ARJUNA012273: ShadowingStore::write_state() - openAndLock failed for /Users/marklittle/github/TransactionalVert.x/echo/ObjectStore/ShadowingStore//StateManager/LockManager/EchoClient.SampleLockable/LockStore/StateManager/LockManager/EchoClient.SampleLockable/0_ffffc0a8000f_e84e_5325d6d2_0 
ARJUNA015029: LockManager::unloadState() failed to write new state for object 0:ffffc0a8000f:e84e:5325d6d2:0 of type /StateManager/LockManager/EchoClient.SampleLockable 
ARJUNA015035: LockManager::setlock() cannot save new lock states 
Net client sending: hello41
Exception in Java verticle 
org.jboss.stm.LockException: Thread[vert.x-eventloop-thread-1,5,main] could not set LockMode.READ lock. Got: LockResult.REFUSED
	at org.jboss.stm.internal.reflect.InvocationHandler.invoke(InvocationHandler.java:344)
	at $Proxy8.value(Unknown Source)
	at EchoClient$1.handle(EchoClient.java:122)
	at EchoClient$1.handle(EchoClient.java:76)
	at org.vertx.java.core.net.impl.DefaultNetClient.doConnected(DefaultNetClient.java:393)
	at org.vertx.java.core.net.impl.DefaultNetClient.access$1000(DefaultNetClient.java:42)
	at org.vertx.java.core.net.impl.DefaultNetClient$4.run(DefaultNetClient.java:385)
	at org.vertx.java.core.impl.DefaultContext$3.run(DefaultContext.java:176)
	at org.vertx.java.core.impl.DefaultContext.execute(DefaultContext.java:135)
	at org.vertx.java.core.net.impl.DefaultNetClient.connected(DefaultNetClient.java:383)
	at org.vertx.java.core.net.impl.DefaultNetClient.access$400(DefaultNetClient.java:42)
	at org.vertx.java.core.net.impl.DefaultNetClient$3.operationComplete(DefaultNetClient.java:358)
	at org.vertx.java.core.net.impl.DefaultNetClient$3.operationComplete(DefaultNetClient.java:335)
	at io.netty.util.concurrent.DefaultPromise.notifyListener0(DefaultPromise.java:628)
	at io.netty.util.concurrent.DefaultPromise.notifyListeners0(DefaultPromise.java:593)
	at io.netty.util.concurrent.DefaultPromise.notifyListeners(DefaultPromise.java:550)
	at io.netty.util.concurrent.DefaultPromise.trySuccess(DefaultPromise.java:396)
	at io.netty.channel.DefaultChannelPromise.trySuccess(DefaultChannelPromise.java:82)
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.fulfillConnectPromise(AbstractNioChannel.java:217)
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:242)
	at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:502)
	at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:452)
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:346)
	at io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:101)
	at java.lang.Thread.run(Thread.java:722)

Exception in Java verticle 
org.jboss.stm.LockException: Thread[vert.x-eventloop-thread-1,5,main] could not set LockMode.READ lock. Got: LockResult.REFUSED
	at org.jboss.stm.internal.reflect.InvocationHandler.invoke(InvocationHandler.java:344)
	at $Proxy8.value(Unknown Source)
	at EchoClient$1.handle(EchoClient.java:122)
	at EchoClient$1.handle(EchoClient.java:76)
	at org.vertx.java.core.net.impl.DefaultNetClient.doConnected(DefaultNetClient.java:393)
	at org.vertx.java.core.net.impl.DefaultNetClient.access$1000(DefaultNetClient.java:42)
	at org.vertx.java.core.net.impl.DefaultNetClient$4.run(DefaultNetClient.java:385)
	at org.vertx.java.core.impl.DefaultContext$3.run(DefaultContext.java:176)
	at org.vertx.java.core.impl.DefaultContext.execute(DefaultContext.java:135)
	at org.vertx.java.core.net.impl.DefaultNetClient.connected(DefaultNetClient.java:383)
	at org.vertx.java.core.net.impl.DefaultNetClient.access$400(DefaultNetClient.java:42)
	at org.vertx.java.core.net.impl.DefaultNetClient$3.operationComplete(DefaultNetClient.java:358)
	at org.vertx.java.core.net.impl.DefaultNetClient$3.operationComplete(DefaultNetClient.java:335)
	at io.netty.util.concurrent.DefaultPromise.notifyListener0(DefaultPromise.java:628)
	at io.netty.util.concurrent.DefaultPromise.notifyListeners0(DefaultPromise.java:593)
	at io.netty.util.concurrent.DefaultPromise.notifyListeners(DefaultPromise.java:550)
	at io.netty.util.concurrent.DefaultPromise.trySuccess(DefaultPromise.java:396)
	at io.netty.channel.DefaultChannelPromise.trySuccess(DefaultChannelPromise.java:82)
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.fulfillConnectPromise(AbstractNioChannel.java:217)
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:242)
	at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:502)
	at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:452)
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:346)
	at io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:101)
	at java.lang.Thread.run(Thread.java:722)

Net client sending: hello42
Net client sending: hello43
Net client sending: hello44
Net client sending: hello45
Net client sending: hello46
Net client sending: hello47
Net client sending: hello48
Net client sending: hello49
Net client sending: hello50
Net client receiving: hello41
hello42
hello43
hello44
hello45
hello46
hello47
hello48
hello49
hello50

What's happened here is that one of the clients has completed its work and deleted the locks from the system used to ensure consistency
across JVM instances whilst the other client was trying to use it. The error is detected by the transaction system and it stops any further
work being attempted within that tranasction. Retrying will work though.

Depending upon timing of the various client operations, you may see conflicts occur between each client attempting to update the shared integer. For
instance, if we run 3 concurrent clients then one of the clients has attempted to increment the shared integer whilst the other one already has it
locked, either with a read lock or a write lock. Because we're using transactions, the isolation property prevents any conflicting
updates and forces the other client to abort the transaction. You may see something like:

rorschach:echo marklittle$ vertx run EchoClient.java & vertx run EchoClient.java & vertx run EchoClient.java
[1] 26658
[2] 26659
Succeeded in deploying verticle 
Succeeded in deploying verticle 
Succeeded in deploying verticle 
Object name: 0:ffffc0a8000f:e84e:5325d6d2:0
Object name: 0:ffffc0a8000f:e84e:5325d6d2:0
Object name: 0:ffffc0a8000f:e84e:5325d6d2:0
ARJUNA012163: Starting service com.arjuna.ats.arjuna.recovery.ActionStatusService on port 59686 
ARJUNA012163: Starting service com.arjuna.ats.arjuna.recovery.ActionStatusService on port 59685 
ARJUNA012163: Starting service com.arjuna.ats.arjuna.recovery.ActionStatusService on port 59687 
ARJUNA012337: TransactionStatusManagerItem host: 127.0.0.1 port: 59686 
ARJUNA012337: TransactionStatusManagerItem host: 127.0.0.1 port: 59685 
ARJUNA012337: TransactionStatusManagerItem host: 127.0.0.1 port: 59687 
ARJUNA012170: TransactionStatusManager started on port 59686 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService 
ARJUNA012170: TransactionStatusManager started on port 59685 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService 
ARJUNA012170: TransactionStatusManager started on port 59687 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService 
ARJUNA012273: ShadowingStore::write_state() - openAndLock failed for /Users/marklittle/github/TransactionalVert.x/echo/ObjectStore/ShadowingStore//StateManager/LockManager/EchoClient.SampleLockable/LockStore/StateManager/LockManager/EchoClient.SampleLockable/0_ffffc0a8000f_e84e_5325d6d2_0 
ARJUNA015029: LockManager::unloadState() failed to write new state for object 0:ffffc0a8000f:e84e:5325d6d2:0 of type /StateManager/LockManager/EchoClient.SampleLockable 
ARJUNA015035: LockManager::setlock() cannot save new lock states 
Exception in Java verticle 
org.jboss.stm.LockException: Thread[vert.x-eventloop-thread-1,5,main] could not set LockMode.WRITE lock. Got: LockResult.REFUSED
	at org.jboss.stm.internal.reflect.InvocationHandler.invoke(InvocationHandler.java:344)
	at $Proxy8.increment(Unknown Source)
	at EchoClient$1.handle(EchoClient.java:120)
	at EchoClient$1.handle(EchoClient.java:76)
	at org.vertx.java.core.net.impl.DefaultNetClient.doConnected(DefaultNetClient.java:393)
	at org.vertx.java.core.net.impl.DefaultNetClient.access$1000(DefaultNetClient.java:42)
	at org.vertx.java.core.net.impl.DefaultNetClient$4.run(DefaultNetClient.java:385)
	at org.vertx.java.core.impl.DefaultContext$3.run(DefaultContext.java:176)
	at org.vertx.java.core.impl.DefaultContext.execute(DefaultContext.java:135)
	at org.vertx.java.core.net.impl.DefaultNetClient.connected(DefaultNetClient.java:383)
	at org.vertx.java.core.net.impl.DefaultNetClient.access$400(DefaultNetClient.java:42)
	at org.vertx.java.core.net.impl.DefaultNetClient$3.operationComplete(DefaultNetClient.java:358)
	at org.vertx.java.core.net.impl.DefaultNetClient$3.operationComplete(DefaultNetClient.java:335)
	at io.netty.util.concurrent.DefaultPromise.notifyListener0(DefaultPromise.java:628)
	at io.netty.util.concurrent.DefaultPromise.notifyListeners0(DefaultPromise.java:593)
	at io.netty.util.concurrent.DefaultPromise.notifyListeners(DefaultPromise.java:550)
	at io.netty.util.concurrent.DefaultPromise.trySuccess(DefaultPromise.java:396)
	at io.netty.channel.DefaultChannelPromise.trySuccess(DefaultChannelPromise.java:82)
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.fulfillConnectPromise(AbstractNioChannel.java:217)
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:242)
	at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:502)
	at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:452)
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:346)
	at io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:101)
	at java.lang.Thread.run(Thread.java:722)

Exception in Java verticle 
org.jboss.stm.LockException: Thread[vert.x-eventloop-thread-1,5,main] could not set LockMode.WRITE lock. Got: LockResult.REFUSED
	at org.jboss.stm.internal.reflect.InvocationHandler.invoke(InvocationHandler.java:344)
	at $Proxy8.increment(Unknown Source)
	at EchoClient$1.handle(EchoClient.java:120)
	at EchoClient$1.handle(EchoClient.java:76)
	at org.vertx.java.core.net.impl.DefaultNetClient.doConnected(DefaultNetClient.java:393)
	at org.vertx.java.core.net.impl.DefaultNetClient.access$1000(DefaultNetClient.java:42)
	at org.vertx.java.core.net.impl.DefaultNetClient$4.run(DefaultNetClient.java:385)
	at org.vertx.java.core.impl.DefaultContext$3.run(DefaultContext.java:176)
	at org.vertx.java.core.impl.DefaultContext.execute(DefaultContext.java:135)
	at org.vertx.java.core.net.impl.DefaultNetClient.connected(DefaultNetClient.java:383)
	at org.vertx.java.core.net.impl.DefaultNetClient.access$400(DefaultNetClient.java:42)
	at org.vertx.java.core.net.impl.DefaultNetClient$3.operationComplete(DefaultNetClient.java:358)
	at org.vertx.java.core.net.impl.DefaultNetClient$3.operationComplete(DefaultNetClient.java:335)
	at io.netty.util.concurrent.DefaultPromise.notifyListener0(DefaultPromise.java:628)
	at io.netty.util.concurrent.DefaultPromise.notifyListeners0(DefaultPromise.java:593)
	at io.netty.util.concurrent.DefaultPromise.notifyListeners(DefaultPromise.java:550)
	at io.netty.util.concurrent.DefaultPromise.trySuccess(DefaultPromise.java:396)
	at io.netty.channel.DefaultChannelPromise.trySuccess(DefaultChannelPromise.java:82)
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.fulfillConnectPromise(AbstractNioChannel.java:217)
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:242)
	at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:502)
	at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:452)
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:346)
	at io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:101)
	at java.lang.Thread.run(Thread.java:722)

Net client sending: hello71
Net client sending: hello71
Net client sending: hello72
Net client sending: hello73
Net client sending: hello74
Net client sending: hello75
Net client sending: hello76
Net client sending: hello77
Net client sending: hello78
Net client sending: hello79
Net client sending: hello80
Net client receiving: hello71
hello72
hello73
hello74
hello75
hello76
hello77
hello78
hello79
hello80

com.arjuna.ats.txoj.exceptions.LockStoreException: Persistent store error. 
ARJUNA015033: LockManager::setlock() cannot load existing lock states 
Exception in Java verticle 
org.jboss.stm.LockException: Thread[vert.x-eventloop-thread-1,5,main] could not set LockMode.WRITE lock. Got: LockResult.REFUSED
	at org.jboss.stm.internal.reflect.InvocationHandler.invoke(InvocationHandler.java:344)
	at $Proxy8.increment(Unknown Source)
	at EchoClient$1.handle(EchoClient.java:120)
	at EchoClient$1.handle(EchoClient.java:76)
	at org.vertx.java.core.net.impl.DefaultNetClient.doConnected(DefaultNetClient.java:393)
	at org.vertx.java.core.net.impl.DefaultNetClient.access$1000(DefaultNetClient.java:42)
	at org.vertx.java.core.net.impl.DefaultNetClient$4.run(DefaultNetClient.java:385)
	at org.vertx.java.core.impl.DefaultContext$3.run(DefaultContext.java:176)
	at org.vertx.java.core.impl.DefaultContext.execute(DefaultContext.java:135)
	at org.vertx.java.core.net.impl.DefaultNetClient.connected(DefaultNetClient.java:383)
	at org.vertx.java.core.net.impl.DefaultNetClient.access$400(DefaultNetClient.java:42)
	at org.vertx.java.core.net.impl.DefaultNetClient$3.operationComplete(DefaultNetClient.java:358)
	at org.vertx.java.core.net.impl.DefaultNetClient$3.operationComplete(DefaultNetClient.java:335)
	at io.netty.util.concurrent.DefaultPromise.notifyListener0(DefaultPromise.java:628)
	at io.netty.util.concurrent.DefaultPromise.notifyListeners0(DefaultPromise.java:593)
	at io.netty.util.concurrent.DefaultPromise.notifyListeners(DefaultPromise.java:550)
	at io.netty.util.concurrent.DefaultPromise.trySuccess(DefaultPromise.java:396)
	at io.netty.channel.DefaultChannelPromise.trySuccess(DefaultChannelPromise.java:82)
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.fulfillConnectPromise(AbstractNioChannel.java:217)
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:242)
	at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:502)
	at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:452)
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:346)
	at io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:101)
	at java.lang.Thread.run(Thread.java:722)

Exception in Java verticle 
org.jboss.stm.LockException: Thread[vert.x-eventloop-thread-1,5,main] could not set LockMode.WRITE lock. Got: LockResult.REFUSED
	at org.jboss.stm.internal.reflect.InvocationHandler.invoke(InvocationHandler.java:344)
	at $Proxy8.increment(Unknown Source)
	at EchoClient$1.handle(EchoClient.java:120)
	at EchoClient$1.handle(EchoClient.java:76)
	at org.vertx.java.core.net.impl.DefaultNetClient.doConnected(DefaultNetClient.java:393)
	at org.vertx.java.core.net.impl.DefaultNetClient.access$1000(DefaultNetClient.java:42)
	at org.vertx.java.core.net.impl.DefaultNetClient$4.run(DefaultNetClient.java:385)
	at org.vertx.java.core.impl.DefaultContext$3.run(DefaultContext.java:176)
	at org.vertx.java.core.impl.DefaultContext.execute(DefaultContext.java:135)
	at org.vertx.java.core.net.impl.DefaultNetClient.connected(DefaultNetClient.java:383)
	at org.vertx.java.core.net.impl.DefaultNetClient.access$400(DefaultNetClient.java:42)
	at org.vertx.java.core.net.impl.DefaultNetClient$3.operationComplete(DefaultNetClient.java:358)
	at org.vertx.java.core.net.impl.DefaultNetClient$3.operationComplete(DefaultNetClient.java:335)
	at io.netty.util.concurrent.DefaultPromise.notifyListener0(DefaultPromise.java:628)
	at io.netty.util.concurrent.DefaultPromise.notifyListeners0(DefaultPromise.java:593)
	at io.netty.util.concurrent.DefaultPromise.notifyListeners(DefaultPromise.java:550)
	at io.netty.util.concurrent.DefaultPromise.trySuccess(DefaultPromise.java:396)
	at io.netty.channel.DefaultChannelPromise.trySuccess(DefaultChannelPromise.java:82)
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.fulfillConnectPromise(AbstractNioChannel.java:217)
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:242)
	at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:502)
	at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:452)
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:346)
	at io.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:101)
	at java.lang.Thread.run(Thread.java:722)

Net client receiving: hello71