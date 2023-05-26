#!/bin/sh
#
# SPDX short identifier: Apache-2.0
#

# Setup the environment for the JBoss Transaction Service

"$JAVA_HOME/bin/java" com.arjuna.ats.jts.TransactionServer -test
