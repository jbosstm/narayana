JBoss, Home of Professional Open Source Copyright 2008, Red Hat Middleware 
LLC, and others contributors as indicated by the @authors tag. All rights 
reserved. See the copyright.txt in the distribution for a full listing of 
individual contributors. This copyrighted material is made available to anyone 
wishing to use, modify, copy, or redistribute it subject to the terms and 
conditions of the GNU Lesser General Public License, v. 2.1. This program 
is distributed in the hope that it will be useful, but WITHOUT A WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
PURPOSE. See the GNU Lesser General Public License for more details. You 
should have received a copy of the GNU Lesser General Public License, v.2.1 
along with this distribution; if not, write to the Free Software Foundation, 
Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA. 


OVERVIEW
--------
This shows how to develop an EJB with transactional attributes and a JPA entity
bean and then test it using TS deployed in JBossAS using Arquillian


USAGE
-----
export JBOSS_HOME=<PATH_TO_JBOSS_HOME>
mvn install -Parq-jbossas-managed (This command will build and test the application using Arquillian)
NOTE: If you get the following you have not exported JBOSS_HOME:
Tests in error: 
  TestBusinessLogic: jbossHome 'null' must exist


DEPLOYING THE QUICKSTART
------------------------
mvn compile jboss-as:deploy (This command will deploy the application without running the tests)

Once the application is deployed, you can access it from a browser by:
http://localhost:8080/jee_transactional_app/ 


EXPECTED OUTPUT
---------------
As well as the normal output you would expect to see from maven, you should also see:
1. A JBossAS instance starting up under mavens control
	15:57:59,754 INFO  [ContainerRegistryCreator] Could not read active container configuration: null
	15:58:00,255 INFO  [AbstractJBossASServerBase] Server Configuration:
	<SNIP/>
	15:58:22,005 INFO  [org.jboss.bootstrap.impl.base.server.AbstractServer] JBossAS [6.0.0.Final "Neo"] Started in 21s:739ms
2. Output from the maven test showing that all the tests ran OK


Some of the tests test that you can't do incorrect things with the bean, hence you may see stack traces in the output. This is normal and may be safely ignored by the user. Here is one example:
10:48:11,228 WARN  [com.arjuna.ats.arjuna] (RMI TCP Connection(2)-127.0.0.1) ARJUNA12125: TwoPhaseCoordinator.beforeCompletion - failed for SynchronizationImple< 0:ffff7f000001:-797207b2:4e673dd2:67, org.hibernate.engine.transaction.synchronization.internal.RegisteredSynchronization@2515272a >: javax.persistence.PersistenceException: org.hibernate.exception.ConstraintViolationException: Unique index or primary key violation: "CONSTRAINT_INDEX_5 ON PUBLIC.CUSTOMER(NAME)"; SQL statement:
insert into Customer (name, id) values (?, ?) [23001-145]
	at org.hibernate.ejb.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1344) [hibernate-entitymanager-4.0.0.Beta5.jar:4.0.0.Beta5]
	at org.hibernate.ejb.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1277) [hibernate-entitymanager-4.0.0.Beta5.jar:4.0.0.Beta5]
	at org.hibernate.ejb.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1283) [hibernate-entitymanager-4.0.0.Beta5.jar:4.0.0.Beta5]
	at org.hibernate.ejb.AbstractEntityManagerImpl$CallbackExceptionMapperImpl.mapManagedFlushFailure(AbstractEntityManagerImpl.java:1454) [hibernate-entitymanager-4.0.0.Beta5.jar:4.0.0.Beta5]
	at org.hibernate.engine.transaction.synchronization.internal.SynchronizationCallbackCoordinatorImpl.beforeCompletion(SynchronizationCallbackCoordinatorImpl.java:109) [hibernate-core-4.0.0.Beta5.jar:4.0.0.Beta5]
	at org.hibernate.engine.transaction.synchronization.internal.RegisteredSynchronization.beforeCompletion(RegisteredSynchronization.java:54) [hibernate-core-4.0.0.Beta5.jar:4.0.0.Beta5]
	at com.arjuna.ats.internal.jta.resources.arjunacore.SynchronizationImple.beforeCompletion(SynchronizationImple.java:97) [jbossjts-4.15.3.Final.jar:]
	at com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator.beforeCompletion(TwoPhaseCoordinator.java:274) [jbossjts-4.15.3.Final.jar:]
	at com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator.end(TwoPhaseCoordinator.java:94) [jbossjts-4.15.3.Final.jar:]
	at com.arjuna.ats.arjuna.AtomicAction.commit(AtomicAction.java:159) [jbossjts-4.15.3.Final.jar:]
	at com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple.commitAndDisassociate(TransactionImple.java:1159) [jbossjts-4.15.3.Final.jar:]
	at com.arjuna.ats.internal.jta.transaction.arjunacore.BaseTransaction.commit(BaseTransaction.java:119) [jbossjts-4.15.3.Final.jar:]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [:1.6.0_22]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57) [:1.6.0_22]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) [:1.6.0_22]
	at java.lang.reflect.Method.invoke(Method.java:616) [:1.6.0_22]
	at org.jboss.weld.util.reflection.SecureReflections$13.work(SecureReflections.java:305) [weld-core-1.1.2.Final.jar:2011-07-26 15:02]
	at org.jboss.weld.util.reflection.SecureReflectionAccess.run(SecureReflectionAccess.java:54) [weld-core-1.1.2.Final.jar:2011-07-26 15:02]
	at org.jboss.weld.util.reflection.SecureReflectionAccess.runAsInvocation(SecureReflectionAccess.java:163) [weld-core-1.1.2.Final.jar:2011-07-26 15:02]
	at org.jboss.weld.util.reflection.SecureReflections.invoke(SecureReflections.java:299) [weld-core-1.1.2.Final.jar:2011-07-26 15:02]
	at org.jboss.weld.bean.builtin.CallableMethodHandler.invoke(CallableMethodHandler.java:57) [weld-core-1.1.2.Final.jar:2011-07-26 15:02]
	at org.jboss.weld.bean.proxy.EnterpriseTargetBeanInstance.invoke(EnterpriseTargetBeanInstance.java:62) [weld-core-1.1.2.Final.jar:2011-07-26 15:02]
	at org.jboss.weld.bean.proxy.ProxyMethodHandler.invoke(ProxyMethodHandler.java:125) [weld-core-1.1.2.Final.jar:2011-07-26 15:02]
	at org.jboss.weldx.transaction.UserTransaction$-294104632$Proxy$_$$_Weld$Proxy$.commit(UserTransaction$-294104632$Proxy$_$$_Weld$Proxy$.java) [weld-core-1.1.2.Final.jar:]
	at org.jboss.narayana.quickstarts.jsf.CustomerManagerManagedBean.addCustomer(CustomerManagerManagedBean.java:66) [classes:]
	at TestManagedBeanCustomerManager.testCustomerCountInPresenceOfRollback(TestManagedBeanCustomerManager.java:126)	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [:1.6.0_22]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57) [:1.6.0_22]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) [:1.6.0_22]
	at java.lang.reflect.Method.invoke(Method.java:616) [:1.6.0_22]
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:44) [arquillian-service:]
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15) [arquillian-service:]
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:41) [arquillian-service:]
	at org.jboss.arquillian.junit.Arquillian$6$1.invoke(Arquillian.java:246) [arquillian-service:]
	at org.jboss.arquillian.container.test.impl.execution.LocalTestExecuter.execute(LocalTestExecuter.java:60) [arquillian-service:]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [:1.6.0_22]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57) [:1.6.0_22]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) [:1.6.0_22]
	at java.lang.reflect.Method.invoke(Method.java:616) [:1.6.0_22]
	at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:90) [arquillian-service:]
	at org.jboss.arquillian.core.impl.EventContextImpl.invokeObservers(EventContextImpl.java:99) [arquillian-service:]
	at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:81) [arquillian-service:]
	at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:134) [arquillian-service:]
	at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:114) [arquillian-service:]
	at org.jboss.arquillian.core.impl.EventImpl.fire(EventImpl.java:67) [arquillian-service:]
	at org.jboss.arquillian.container.test.impl.execution.ContainerTestExecuter.execute(ContainerTestExecuter.java:38) [arquillian-service:]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [:1.6.0_22]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57) [:1.6.0_22]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) [:1.6.0_22]
	at java.lang.reflect.Method.invoke(Method.java:616) [:1.6.0_22]
	at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:90) [arquillian-service:]
	at org.jboss.arquillian.core.impl.EventContextImpl.invokeObservers(EventContextImpl.java:99) [arquillian-service:]
	at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:81) [arquillian-service:]
	at org.jboss.arquillian.test.impl.TestContextHandler.createTestContext(TestContextHandler.java:82) [arquillian-service:]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [:1.6.0_22]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57) [:1.6.0_22]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) [:1.6.0_22]
	at java.lang.reflect.Method.invoke(Method.java:616) [:1.6.0_22]
	at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:90) [arquillian-service:]
	at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88) [arquillian-service:]
	at org.jboss.arquillian.test.impl.TestContextHandler.createClassContext(TestContextHandler.java:68) [arquillian-service:]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [:1.6.0_22]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57) [:1.6.0_22]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) [:1.6.0_22]
	at java.lang.reflect.Method.invoke(Method.java:616) [:1.6.0_22]
	at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:90) [arquillian-service:]
	at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88) [arquillian-service:]
	at org.jboss.arquillian.test.impl.TestContextHandler.createSuiteContext(TestContextHandler.java:54) [arquillian-service:]
	at sun.reflect.GeneratedMethodAccessor1.invoke(Unknown Source) [:1.6.0_22]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) [:1.6.0_22]
	at java.lang.reflect.Method.invoke(Method.java:616) [:1.6.0_22]
	at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:90) [arquillian-service:]
	at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88) [arquillian-service:]
	at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:134) [arquillian-service:]
	at org.jboss.arquillian.test.impl.EventTestRunnerAdaptor.test(EventTestRunnerAdaptor.java:111) [arquillian-service:]
	at org.jboss.arquillian.junit.Arquillian$6.evaluate(Arquillian.java:239) [arquillian-service:]
	at org.jboss.arquillian.junit.Arquillian$4.evaluate(Arquillian.java:202) [arquillian-service:]
	at org.jboss.arquillian.junit.Arquillian.multiExecute(Arquillian.java:290) [arquillian-service:]
	at org.jboss.arquillian.junit.Arquillian.access$100(Arquillian.java:45) [arquillian-service:]
	at org.jboss.arquillian.junit.Arquillian$5.evaluate(Arquillian.java:216) [arquillian-service:]
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:76) [arquillian-service:]
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50) [arquillian-service:]
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:193) [arquillian-service:]
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52) [arquillian-service:]
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191) [arquillian-service:]
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:42) [arquillian-service:]
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184) [arquillian-service:]
	at org.jboss.arquillian.junit.Arquillian$2.evaluate(Arquillian.java:161) [arquillian-service:]
	at org.jboss.arquillian.junit.Arquillian.multiExecute(Arquillian.java:290) [arquillian-service:]
	at org.jboss.arquillian.junit.Arquillian.access$100(Arquillian.java:45) [arquillian-service:]
	at org.jboss.arquillian.junit.Arquillian$3.evaluate(Arquillian.java:175) [arquillian-service:]
	at org.junit.runners.ParentRunner.run(ParentRunner.java:236) [arquillian-service:]
	at org.jboss.arquillian.junit.Arquillian.run(Arquillian.java:123) [arquillian-service:]
	at org.junit.runner.JUnitCore.run(JUnitCore.java:157) [arquillian-service:]
	at org.junit.runner.JUnitCore.run(JUnitCore.java:136) [arquillian-service:]
	at org.jboss.arquillian.junit.container.JUnitTestRunner.execute(JUnitTestRunner.java:65) [arquillian-service:]
	at org.jboss.arquillian.protocol.jmx.JMXTestRunner.runTestMethodInternal(JMXTestRunner.java:152) [arquillian-service:]
	at org.jboss.arquillian.protocol.jmx.JMXTestRunner.runTestMethodRemote(JMXTestRunner.java:112) [arquillian-service:]
	at org.jboss.as.arquillian.service.ArquillianService$ExtendedJMXTestRunner.runTestMethodRemote(ArquillianService.java:205) [arquillian-service:]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [:1.6.0_22]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57) [:1.6.0_22]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) [:1.6.0_22]
	at java.lang.reflect.Method.invoke(Method.java:616) [:1.6.0_22]
	at com.sun.jmx.mbeanserver.StandardMBeanIntrospector.invokeM2(StandardMBeanIntrospector.java:111) [:1.6.0_22]
	at com.sun.jmx.mbeanserver.StandardMBeanIntrospector.invokeM2(StandardMBeanIntrospector.java:45) [:1.6.0_22]
	at com.sun.jmx.mbeanserver.MBeanIntrospector.invokeM(MBeanIntrospector.java:226) [:1.6.0_22]
	at com.sun.jmx.mbeanserver.PerInterface.invoke(PerInterface.java:138) [:1.6.0_22]
	at com.sun.jmx.mbeanserver.MBeanSupport.invoke(MBeanSupport.java:251) [:1.6.0_22]
	at com.sun.jmx.interceptor.DefaultMBeanServerInterceptor.invoke(DefaultMBeanServerInterceptor.java:857) [:1.6.0_22]
	at com.sun.jmx.mbeanserver.JmxMBeanServer.invoke(JmxMBeanServer.java:795) [:1.6.0_22]
	at org.jboss.as.jmx.tcl.TcclMBeanServer.invoke(TcclMBeanServer.java:214)
	at javax.management.remote.rmi.RMIConnectionImpl.doOperation(RMIConnectionImpl.java:1450) [:1.6.0_22]
	at javax.management.remote.rmi.RMIConnectionImpl.access$200(RMIConnectionImpl.java:90) [:1.6.0_22]
	at javax.management.remote.rmi.RMIConnectionImpl$PrivilegedOperation.run(RMIConnectionImpl.java:1285) [:1.6.0_22]
	at javax.management.remote.rmi.RMIConnectionImpl.doPrivilegedOperation(RMIConnectionImpl.java:1383) [:1.6.0_22]
	at javax.management.remote.rmi.RMIConnectionImpl.invoke(RMIConnectionImpl.java:807) [:1.6.0_22]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [:1.6.0_22]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57) [:1.6.0_22]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) [:1.6.0_22]
	at java.lang.reflect.Method.invoke(Method.java:616) [:1.6.0_22]
	at sun.rmi.server.UnicastServerRef.dispatch(UnicastServerRef.java:322) [:1.6.0_22]
	at sun.rmi.transport.Transport$1.run(Transport.java:177) [:1.6.0_22]
	at java.security.AccessController.doPrivileged(Native Method) [:1.6.0_22]
	at sun.rmi.transport.Transport.serviceCall(Transport.java:173) [:1.6.0_22]
	at sun.rmi.transport.tcp.TCPTransport.handleMessages(TCPTransport.java:553) [:1.6.0_22]
	at sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.run0(TCPTransport.java:808) [:1.6.0_22]
	at sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.run(TCPTransport.java:667) [:1.6.0_22]
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1110) [:1.6.0_22]
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:603) [:1.6.0_22]
	at java.lang.Thread.run(Thread.java:679) [:1.6.0_22]
