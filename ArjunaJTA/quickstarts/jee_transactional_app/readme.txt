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
mvn clean install


COMMON ERROR
------------
If you get the following you have not exported JBOSS_HOME:
Tests in error: 
  TestBusinessLogic: jbossHome 'null' must exist


EXPECTED OUTPUT
---------------
As well as the normal output you would expect to see from maven, you should also see:
1. A JBossAS instance starting up under mavens control
	15:57:59,754 INFO  [ContainerRegistryCreator] Could not read active container configuration: null
	15:58:00,255 INFO  [AbstractJBossASServerBase] Server Configuration:
	<SNIP/>
	15:58:22,005 INFO  [org.jboss.bootstrap.impl.base.server.AbstractServer] JBossAS [6.0.0.Final "Neo"] Started in 21s:739ms
2. Output from the maven test showing that all the tests ran OK