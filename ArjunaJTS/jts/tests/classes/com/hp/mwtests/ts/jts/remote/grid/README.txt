JBoss, Home of Professional Open Source
Copyright 2006, Red Hat Middleware LLC, and individual contributors
as indicated by the @author tags. 
See the copyright.txt in the distribution for a full listing 
of individual contributors.
This copyrighted material is made available to anyone wishing to use,
modify, copy, or redistribute it subject to the terms and conditions
of the GNU Lesser General Public License, v. 2.1.
This program is distributed in the hope that it will be useful, but WITHOUT A
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License,
v.2.1 along with this distribution; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
MA  02110-1301, USA.


(C) 2005-2006,
@author JBoss Inc.
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
