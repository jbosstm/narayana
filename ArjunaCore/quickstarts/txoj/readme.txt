JBoss, Home of Professional Open Source Copyright 2008, Red Hat Middleware 
LLC, and others contributors as indicated by the @authors tag. All rights 
reserved. See the copyright.txt in the distribution for a full listing of 
individual contributors. This copyrighted material is made available to anyone 
wishing to use, modify, copy, or redistribute it subject to the terms and 
conditions of the GNU Lesser General Public License, v. 2.1. This program 
is distributed in the hope that it will be useful, but WITHOUT A WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
PURPOSE. See the GNU Lesser General Public License for more details. You 
should have received a copy of the GNU Lesser General Public License, v.2.1 
along with this distribution; if not, write to the Free Software Foundation, 
Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA. 


OVERVIEW
--------
This shows an example of how to introduce TXOJ to your project.

TXOJ are described in the ArjunaCore developers guide.


USAGE
-----
mvn compile exec:exec

OR
--
./run.[sh|bat]


EXPECTED OUTPUT
---------------
As well as the maven output you would normally expect, you should also see the following:

9-Jun-2011 17:24:51 com.arjuna.ats.arjuna.recovery.TransactionStatusManager addService
INFO: ARJUNA12163: Starting service com.arjuna.ats.arjuna.recovery.ActionStatusService on port 44448
29-Jun-2011 17:24:51 com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem <init>
INFO: ARJUNA12337: TransactionStatusManagerItem host: 127.0.0.1 port: 44448
29-Jun-2011 17:24:51 com.arjuna.ats.arjuna.recovery.TransactionStatusManager start
INFO: ARJUNA12170: TransactionStatusManager started on port 44448 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService
Created persistent object 0:ffff7f000001:ac45:4e0b51d3:0
Atomic object operated as expected


WHAT JUST HAPPENED?
-------------------
We created a transactional object and modified its state in an ACI property transaction (Atomic, Consistent and Isolated, but not Durable)
