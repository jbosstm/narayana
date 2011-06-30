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


	There are 3 object store examples:

	1. A transaction manager stores its commit decision after before deciding to commit a transaction.
	   This example shows how to change the store type to an (unsafe) in memory store:
		org.jboss.narayana.jta.quickstarts.VolatileStoreExample
	2. This example shows how to change the store type to use the fast Hornetq journal:
		org.jboss.narayana.jta.quickstarts.HornetqStoreExample
	3. This example shows how to change the store type to a file base store but in directory different from the default:
		org.jboss.narayana.jta.quickstarts.FileStoreTest

	When running an example an exit code of zero represents success (otherwise failure together with an exception trace)

	To run an example use the maven java exec plugin:
	mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.VolatileStoreExample
	mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.HornetqStoreExample
	mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.FileStoreTest
