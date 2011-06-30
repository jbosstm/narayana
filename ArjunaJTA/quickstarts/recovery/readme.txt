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


    There are 3 recovery examples:

    1. An example showing how to manually enlist (2 dummy) resources (without failures)
		org.jboss.narayana.jta.examples.recovery.BasicXAExample

	2. An example demonstrating recovery from failures after prepare but before commit using Dummy XA resources:
		org.jboss.narayana.jta.examples.recovery.DummyRecovery

	   This example needs to be run twice the first run (controled by a command line arg of -f, for fail)
	   will halt the VM thus generating a "recovery record".

	   The second run (controled by a command line arg of -r, for recover) will cause the transaction manager
	   recovery system to replay the commit phase of the transaction.

	3. Another example demonstrating recovery from failures but using JMS XA resources (instead of dummy ones).
		Again the first run will generate 2 messages and leave the transaction dangling in the prepared state.
		The second run will trigger the recovery system which in turn commits the two prepared JMS XA resources.
		Then the two messages are consumed.

    When running an example an exit code of zero represents success (otherwise failure together with an exception trace).

    To run an example use the maven java exec pluging. For example to run the second recovery example:

	  mvn -e exec:java -Dexec.mainClass=org.jboss.narayana.jta.examples.recovery.DummyRecovery -Dexec.args="-f"
	  mvn -e exec:java -Dexec.mainClass=org.jboss.narayana.jta.examples.recovery.DummyRecovery -Dexec.args="-r"

    And to run the JMS recovery example:

	  mvn -e exec:java -Dexec.mainClass=org.jboss.narayana.jta.examples.recovery.JmsRecovery -Dexec.args="-f"
	  mvn -e exec:java -Dexec.mainClass=org.jboss.narayana.jta.examples.recovery.JmsRecovery -Dexec.args="-r"

	On the second step of each example you will see a warning
	   (HornetQException[errorCode=4 message=The connection was disconnected because of server shutdown])
	which can be ignored. It caused by the recovery subsystem not performing an orderly shutdown of JMS
	communications. This will be fixed in the next revision of JBossTS.

	And finally, to read the (binary) hornetq logs use the PrintData tool:

	mvn -e exec:java -Dexec.mainClass=org.hornetq.core.persistence.impl.journal.PrintData -Dexec.classpathScope=test -Dexec.args="target/data/hornetq/bindings target/data/hornetq/largemessages"
