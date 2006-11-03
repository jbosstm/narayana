#
# JBoss, Home of Professional Open Source
# Copyright 2006, Red Hat Middleware LLC, and individual contributors
# as indicated by the @author tags. 
# See the copyright.txt in the distribution for a full listing 
# of individual contributors.
# This copyrighted material is made available to anyone wishing to use,
# modify, copy, or redistribute it subject to the terms and conditions
# of the GNU General Public License, v. 2.0.
# This program is distributed in the hope that it will be useful, but WITHOUT A 
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
# PARTICULAR PURPOSE.  See the GNU General Public License for more details.
# You should have received a copy of the GNU General Public License,
# v. 2.0 along with this distribution; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
# MA  02110-1301, USA.
# 
# (C) 2005-2006,
# @author JBoss Inc.
#
To create a stand-alone coordinator:

(1) create a separate installation of your application server to the one you
will use for your clients/services.

(2) edit the build.xml to point to this installation.

You need to set hostname, port and deploy.dir parameters to those for the
application server used for the stand-alone coordinator.

(3) run ant with the deploy target that matches your application server
(deploy-webmethods, deploy-weblogic or deploy-jboss).

(4) when running clients and services, edit the wstx.xml file in their
deployment to add

	    <property
	    name="com.arjuna.mw.wst.coordinatorURL"
	    value="http://@hostname@:@port@/xts/soap/ActivationCoordinator"/>

	    <property
	    name="com.arjuna.mw.wst.ba.terminatorURL"
	    value="http://@hostname@:@port@/xts/soap/BusinessActivityTerminatorParticipant"/>

where hostname and port are replaced with the appropriate values.

(5) make sure you start  your coordinator deployment before any clients or
services that may want to use it.
