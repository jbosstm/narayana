#
# SPDX short identifier: Apache-2.0
#

These tests  check that transactions   which have timeouts  associated
with them when they are created are  rolled back if the timeout period
elapses. They also  check that the default  timeout (the meaning of  0
for the transaction timeout) is configurable.

When running TerminationTest,  set TX_REAPER_TIMEOUT property variable
to 1000.

When running DefaultTimeout, set the DEFAULT_TIMEOUT property variable
to 2, and do not unset TX_REAPER_TIMEOUT.

expected_output1: TerminationTest
