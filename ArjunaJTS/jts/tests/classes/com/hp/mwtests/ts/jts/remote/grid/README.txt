#
# SPDX short identifier: Apache-2.0
#

This test illustrates the use of a Resource  to control the operations
on an  atomic object.  In  the example the  atomic object derives from
Resource.  However, this should not happen in general, and in fact the
OTS specification warns against it: a  Resource can only be registered
in one action at  a time, so  a  transactional object inheriting  from
Resource and registering  itself can only  be  in one action. This  is
because when a  Resource is told to commit/abort  it cannot tell which
transaction the command is issued on behalf  of. The best way of doing
this is to have Resource objects  which drive the transactional object
directly a la AbstractRecords in Arjuna.

This  example uses the factory   to create transactions, and  explicit
context progagation.

Server: GridServer *and* the transaction manager (OTS).
