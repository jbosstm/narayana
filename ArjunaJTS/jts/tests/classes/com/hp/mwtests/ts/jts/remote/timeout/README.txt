#
# SPDX short identifier: Apache-2.0
#

These tests  check that transactions   which have timeouts  associated
with them when they are created are  rolled back if the timeout period
elapses. They also  check that the default  timeout (the meaning of  0
for the transaction timeout) is configurable.

When running TimeoutClient, set TX_REAPER_TIMEOUT property variable to
1000  at  the server  only  (-DTX_REAPER_TIMEOUT=1000  on the  command
line).  Run SetGetServer first.

expected_output: from TimeoutClient
