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
This    example      demonstrates   the    difference    between    an
ArjunaSubtranAwareResource   and   the  standard   OTS   version.   An
ArjunaSubtranAwareResource  always  participates  within  a  two-phase
commit protocol for  nested  transactions.  Propagation occurs  in the
same way,  however,  i.e., if registered using  register_subtran_aware
then the  resource    is  not propagated  to  the    parent;  if using
register_resource then  it   will be   propagated. For   each   nested
transaction, it will take part in the two-phase protocol.

Not a remote test.

expected_output1: from ArjunaNestingTest

expected_output2: from ArjunaNestingTest -subtran

