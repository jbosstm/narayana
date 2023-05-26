#
# SPDX short identifier: Apache-2.0
#

This tests the use of a separate transaction manager server with AIT.

Server: the  transaction    server, and HammerServer with   -server  1
option.

It is possible   to run two clients  for  the same object to  observer
concurrency control issues. To do so, run one with the -slave option.
