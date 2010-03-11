/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package org.jboss.jbossts.txbridge.tests.inbound.utility;

import org.apache.log4j.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Implementation of XAResource for use in txbridge recovery tests.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-01
 */
public class TestXAResourceRecovered extends TestXAResource implements XAResource
{
    private static Logger log = Logger.getLogger(TestXAResourceRecovered.class);

    @Override
    public void rollback(Xid xid) throws XAException
    {
        log.trace("TestXAResourceRecovered.rollback(Xid="+xid+")");

        TestXAResourceRecoveryHelper.getInstance().removeLog(xid);
    }

    @Override
    public void commit(Xid xid, boolean b) throws XAException
    {
        log.trace("TestXAResourceRecovered.commit(Xid="+xid+", b="+b+")");

        TestXAResourceRecoveryHelper.getInstance().removeLog(xid);
    }

    @Override
    public Xid[] recover(int i) throws XAException {
        log.trace("TestXAResourceRecovered.recover(i="+i+")");
        
        return TestXAResourceRecoveryHelper.getInstance().recover();
    }

    @Override
    public void forget(Xid xid) throws XAException {
        log.trace("TestXAResource.forget(Xid="+xid+")");

        TestXAResourceRecoveryHelper.getInstance().removeLog(xid);
    }

}
