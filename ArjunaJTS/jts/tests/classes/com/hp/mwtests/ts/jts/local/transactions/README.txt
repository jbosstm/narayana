#
# SPDX short identifier: Apache-2.0
#

TransactionTest1: Tries to terminate transactions out of order.

TransactionTest2: Tests  garbage collection. Will  run silently  for a
while until it cannot create any  more transactions, and then rollback
all created transactions.

expected_output1: from TransactionTest1
