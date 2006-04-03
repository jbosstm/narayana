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
To      enable    checked    transactions,       either      set   the
OTS_CHECKED_TRANSACTIONS=YES property variable, or run the test script
with the -check option.

In addition to testing checked  transactions, this test also shows how
to override the CheckedAction implementation. No warning message about
the number of active  threads should be  printed when  the transaction
terminates, as is the default behaviour.

expected_output1: without checked transactions enabled.

expected_output2: with checked transactions enabled.
