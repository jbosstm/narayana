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
A transaction manager (TM) must be able to recover from failures during the commit phase of a transaction. These examples
demonstrate that the TM is able to recover failed transactions.

There are three examples:

    1. An example showing how to manually enlist (2 dummy) resources (without failures)
		org.jboss.narayana.jta.quickstarts.recovery.BasicXAExample

	2. An example demonstrating recovery from failures after prepare but before commit using Dummy XA resources:
		org.jboss.narayana.jta.quickstarts.recovery.DummyRecovery

	   This example needs to be run twice, the first run (controled by a command line arg of -f, for fail)
	   will halt the VM thus generating a "recovery record".

	   The second run (controled by a command line arg of -r, for recover) will cause the transaction manager
	   recovery system to replay the commit phase of the transaction.

	3. Another example demonstrating recovery from failures but using JMS XA resources (instead of dummy ones).
		Again the first run will generate 2 messages and leave the transaction dangling in the prepared state.
		The second run will trigger the recovery system which in turn commits the two prepared JMS XA resources.
		Then the two messages are consumed.


USAGE
-----
./run.[sh|bat]

To run an example manually you will need to run the example twice, once with a flag to tell the example to
generate a failure followed by a second run with a flag to tell the example to recover the failed transaction.
For example to run the JMS example:
	  mvn exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.JmsRecovery -Dexec.args="-f"
	  mvn exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.JmsRecovery -Dexec.args="-r"

and to run the Dummy XA resource example:
	  mvn exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.DummyRecovery -Dexec.args="-f"
	  mvn exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.DummyRecovery -Dexec.args="-r"

To run the example showing how to manually enlist resources into a transaction and to commit without failures:
	  mvn exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.recovery.BasicXAExample


	which can be ignored. It caused by the recovery subsystem not performing an orderly shutdown of JMS
	communications. This will be fixed in the next revision of JBossTS.

	And finally, to read the (binary) hornetq logs use the PrintData tool:

	mvn exec:java -Dexec.mainClass=org.hornetq.core.persistence.impl.journal.PrintData -Dexec.classpathScope=test -Dexec.args="target/data/hornetq/bindings target/data/hornetq/largemessages"

	Pass a -e flag to the mvn command line to enable exception stack traces.

EXPECTED OUTPUT
---------------
Running all the examples will generate a lot of output including a java stack trace. The first step of each example
is to cause the VM to halt thus generating a recovery record. The second step is meant to demonstrate the recovery
system recovering the transaction.

If all the examples succeed the last line of output should be
"All recovery examples succeeded"

When the dummy example finishes look for the output
"Dummy example succeeded"
When the dummy example finishes look for the output
"JMS example succeeded"

The final step of the JMS example will output the message
"WARNING: I'm closing a core ClientSession you left open."
and then generate a stack trace to show where the ClientSession was originally opened. This problem can be ignored but
will be fixed in the next release of the transaction manager product. The error is caused by the recovery subsystem
not performing an orderly shutdown of JMS communications.

WHAT JUST HAPPENED?
-------------------

The JMS example starts a transaction and enlists two resources. One of the resources is a JMS XA session that 
The JMS example generates two messages within a transactional XA session. Before sending the messages two
resources are enlisted into the transaction, an XA resource corresponding to the JMS session used to send the 
messages and a dummy XA resource. On the first run the dummy resources is configured to halt the VM during
the second, commit, phase of the transaction completion protocol. This will cause the JMS messages to require
recovery.

During the second run the example manually requests a "recovery scan" (normally the recovery subsystem runs
automatically). This scan will cause the the two messages to be committed hence making them available for consumption.
The 2 messages are then consumed.

