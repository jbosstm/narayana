#---
# JBoss, Home of Professional Open Source
# Copyright 2007, Red Hat Middleware LLC, and individual contributors
# as indicated by the @author tags.
# See the copyright.txt in the distribution for a
# full listing of individual contributors.
# This copyrighted material is made available to anyone wishing to use,
# modify, copy, or redistribute it subject to the terms and conditions
# of the GNU Lesser General Public License, v. 2.1.
# This program is distributed in the hope that it will be useful, but WITHOUT A
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
# You should have received a copy of the GNU Lesser General Public License,
# v.2.1 along with this distribution; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
# MA  02110-1301, USA.
#
# (C) 2005-2006,
# @author JBoss Inc.
#--- 

if [ ! -d tmp ] ; then
    mkdir tmp
fi

if [ ! -d tmp/classes ] ; then
    mkdir tmp/classes
fi

if [ ! -d tmp/src ] ; then
    mkdir tmp/src
fi

# delete old generated versions -- any changes should be made to a copy!

rm -rf tmp/classes/* tmp/src/*

#$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interop11-binding.wsdl -d tmp/classes -s tmp/src -target 2.1 -p com.jboss.transaction.txinterop.webservices.atinterop.generated wsdl/interopat-initiator-binding.wsdl

#$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interop11-binding.wsdl -d tmp/classes -s tmp/src -target 2.1 -p com.jboss.transaction.txinterop.webservices.atinterop.generated wsdl/interopat-participant-binding.wsdl

#$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interop11-binding.wsdl -d tmp/classes -s tmp/src -target 2.1 -p com.jboss.transaction.txinterop.webservices.bainterop.generated wsdl/interopba-initiator-binding.wsdl

#$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interop11-binding.wsdl -d tmp/classes -s tmp/src -target 2.1 -p com.jboss.transaction.txinterop.webservices.bainterop.generated wsdl/interopba-participant-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interopat-initiator-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 -p com.jboss.transaction.txinterop.webservices.atinterop.generated wsdl/interopat-initiator-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interopat-participant-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 -p com.jboss.transaction.txinterop.webservices.atinterop.generated wsdl/interopat-participant-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interopba-initiator-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 -p com.jboss.transaction.txinterop.webservices.bainterop.generated wsdl/interopba-initiator-binding.wsdl

$GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/interopba-participant-binding.wsdl -d tmp/classes -s tmp/src -target 2.0 -p com.jboss.transaction.txinterop.webservices.bainterop.generated wsdl/interopba-participant-binding.wsdl

