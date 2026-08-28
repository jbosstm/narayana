#
#  JBoss, Home of Professional Open Source
#  Copyright 2009, Red Hat Middleware LLC, and individual contributors
#  as indicated by the @author tags.
#  See the copyright.txt in the distribution for a full listing
#  of individual contributors.
#  This copyrighted material is made available to anyone wishing to use,
#  modify, copy, or redistribute it subject to the terms and conditions
#  of the GNU Lesser General Public License, v. 2.1.
#  This program is distributed in the hope that it will be useful, but WITHOUT A
#  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
#  PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
#  You should have received a copy of the GNU Lesser General Public License,
#  v.2.1 along with this distribution; if not, write to the Free Software
#  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
#  MA  02110-1301, USA.
#
#
#  (C) 2009,
#  @author JBoss Inc.
#
#
#

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

# $JBOSS_HOME/bin/wsconsume.sh -v -k -p org.jboss.jbossts.xts.servicetests -w wsdl/xtsservicetests.wsdl -o tmp/classes -s tmp/src wsdl/xtsservicetests.wsdl

# $JBOSS_HOME/bin/wsconsume.sh -v -k -w wsdl/xtsservicetests.wsdl -o tmp/classes -s tmp/src wsdl/xtsservicetests.wsdl

# $GF_HOME/bin/wsimport -verbose -keep -wsdllocation wsdl/xtsservicetests.wsdl -d tmp/classes -s tmp/src -target 2.1 wsdl/xtsservicetests.wsdl

$GF_HOME/bin/wsimport -verbose -keep -target 2.0 -wsdllocation wsdl/xtsservicetests.wsdl -d tmp/classes -s tmp/src wsdl/xtsservicetests.wsdl
