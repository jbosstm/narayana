export JBOSS_HOME=
cp $JBOSS_HOME/bin/jboss-cli.sh $JBOSS_HOME/bin/jboss-admin.sh
chmod +x $JBOSS_HOME/bin/jboss-admin.sh

Set jboss version in pom.xml for:

<artifactId>jboss-as-arquillian-container-managed</artifactId>
<artifactId>jboss-as-controller-client</artifactId>

Note, the tests may need to be ran one at a time, if the following issue has not been resolved: https://issues.jboss.org/browse/JBTM-1071. See the issue for the work around.

If you do want to run them all at once, run:

mvn test

The raw logs in ./target/log are quite hard to read. Run the following command to create a more human readable set of logs:

java -cp target/classes/ com.arjuna.qa.simplifylogs.SimplifyLogs ./target/log/ ./target/log-simplified

The simplified logs are now in: ./target/log-simplified

== Validating the log output ==
There is a set of Byteman scripts in ./src/test/resources/scripts/. Each script applies to one or more test scenarios. Each valid combination of bytman script to test scenario was executed by the above tests. For example if you look at the 'ATCrashDuringCommit.txt' Byteman script in the 'Available tests include:' section, you will see that the following scenarios are supported:

	org.jboss.jbossts.xts.servicetests.test.at.MultiParticipantPrepareAndCommitTest
	org.jboss.jbossts.xts.servicetests.test.at.MultiServicePrepareAndCommitTest

The output from these tests are named:

	ATCrashDuringCommit.MultiParticipantPrepareAndCommitTest
	ATCrashDuringCommit.MultiServicePrepareAndCommitTest

You now need to look at the expected output for these logs in 'ATCrashDuringCommit.txt'.

For example the expected output is:

	#   prepared received for participant XXXXXX
	#   prepared received for participant XXXXXX
	#   prepared received for participant XXXXXX
	#   JVM exit
	#   created recovered coordinator engine XXXXXX
	#   created recovered coordinator engine XXXXXX
	#   created recovered coordinator engine XXXXXX
	#   commit on recovered coordinator engine XXXXXX
	#   commit on recovered coordinator engine XXXXXX
	#   commit on recovered coordinator engine XXXXXX
	#   removed committed transaction XXXXXX

The simplified logs have converted this into something like this:

	=== Before crash===
	prepare on non recovered coordinator engine 0
	received prepared message for coordinator engine 0
	prepare on non recovered coordinator engine 1
	prepared received for participant 1
	prepare on non recovered coordinator engine 2
	prepared received for participant 2


	=== After crash===
	created recovered coordinator engine 0
	created recovered coordinator engine 1
	created recovered coordinator engine 2
	commit on recovered coordinator engine 0
	commit on recovered coordinator engine 1
	commit on recovered coordinator engine 2
	removed committed transaction 3

Here you can see that the participant IDs have been replaced with unique monotomicly increasing integer IDs starting from zero. The script that simplifies these logs is quite basic, so the transaction ID is also assigned an ID from the same pool. This explains why the log says "removed committed transaction 3" when there is only 1 transaction. These numbers should be treat as unique IDs and no order should be inferred.

You should now be able to check the remainder of the logs, using this method
