#
# SPDX short identifier: Apache-2.0
#

To      enable    checked    transactions,       either      set   the
OTS_CHECKED_TRANSACTIONS=YES property variable, or run the test script
with the -check option.

In addition to testing checked  transactions, this test also shows how
to override the CheckedAction implementation. No warning message about
the number of active  threads should be  printed when  the transaction
terminates, as is the default behaviour.

expected_output1: without checked transactions enabled.

expected_output2: with checked transactions enabled.
