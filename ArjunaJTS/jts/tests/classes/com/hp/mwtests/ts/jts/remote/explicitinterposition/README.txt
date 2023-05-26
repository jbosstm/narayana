#
# SPDX short identifier: Apache-2.0
#

NOTE: If not running with Orbix and running with a transaction manager
server then remember to specify the OTS factory  reference file in the
environment  variable  OTS_TRANSACTION_MANAGER for  the client *only*,
i.e., make  sure  this  is  not  set  for  the  server (since  we want
interposition!)

Server: SetGetServer
