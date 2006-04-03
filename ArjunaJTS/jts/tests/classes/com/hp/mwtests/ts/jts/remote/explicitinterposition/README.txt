JBoss, Home of Professional Open Source
Copyright 2006, JBoss Inc., and individual contributors as indicated
by the @authors tag.  All rights reserved. 
See the copyright.txt in the distribution for a full listing 
of individual contributors.
This copyrighted material is made available to anyone wishing to use,
modify, copy, or redistribute it subject to the terms and conditions
of the GNU General Public License, v. 2.0.
This program is distributed in the hope that it will be useful, but WITHOUT A 
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
PARTICULAR PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License,
v. 2.0 along with this distribution; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
MA  02110-1301, USA.

(C) 2005-2006,
@author JBoss Inc.
NOTE: If not running with Orbix and running with a transaction manager
server then remember to specify the OTS factory  reference file in the
environment  variable  OTS_TRANSACTION_MANAGER for  the client *only*,
i.e., make  sure  this  is  not  set  for  the  server (since  we want
interposition!)

Server: SetGetServer
