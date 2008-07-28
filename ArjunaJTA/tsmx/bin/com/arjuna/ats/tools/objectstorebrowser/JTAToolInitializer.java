/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package com.arjuna.ats.tools.objectstorebrowser;

import com.arjuna.ats.tools.objectstorebrowser.rootprovider.InFlightTransactionPseudoStore;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.UidInfo;
import com.arjuna.ats.tools.toolsframework.plugin.ToolPlugin;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.arjuna.utils.XATxConverter;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.jta.xa.XidImple;

import javax.transaction.xa.Xid;
import javax.transaction.Transaction;
import java.util.Map;

public class JTAToolInitializer implements IToolInitializer
{
    public void initialize(ToolPlugin plugin)
    {
        com.arjuna.ats.internal.jta.Implementations.initialise();   // needed for XAResourceRecord
        
        InFlightTransactionPseudoStore.setTransactionLister(new TransactionLister(){
            public Map<Uid, Transaction> getTransactions()
            {
                return TransactionImple.getTransactions();
            }
        });

        UidInfo.setUidConverter(new UidConverter() {

            public Uid toUid(Xid xid)
            {
                if (xid instanceof XidImple)
                    return XATxConverter.getUid(((XidImple) xid).getXID());

                return null;
            }
        });        
    }
}
