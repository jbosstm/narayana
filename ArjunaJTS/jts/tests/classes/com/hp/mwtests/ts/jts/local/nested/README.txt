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
This test illustrates  the difference  between registering a  Resource
and a  SubtransactionAwareResource   with a nested   transaction.  The
Resource   will  only be   called   during the  top-level  transaction
commit/abort, whereas the SubtransactionAwareResource will be informed
when    the    nested       transaction    terminates.      If     the
SubtranscationAwareResource  was  registered  using  register_resource
(the default in  this example) it  will be propagated to the top-level
transaction; otherwise it will not. Use the -subtran option to see the
differences.

Not a remote test.

expected_output1: from NestedTester

expected_output2: from NestedTester -subtran

expected_output3: from NestedTester -abort
