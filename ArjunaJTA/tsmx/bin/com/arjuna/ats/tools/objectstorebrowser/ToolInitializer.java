package com.arjuna.ats.tools.objectstorebrowser;

import com.arjuna.ats.tools.toolsframework.plugin.ToolPlugin;
import com.arjuna.ats.tools.objectstorebrowser.rootprovider.InFlightTransactionPseudoStore;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.UidInfo;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.internal.arjuna.utils.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

import javax.transaction.Transaction;
import javax.transaction.xa.Xid;
import java.util.Map;

public class ToolInitializer implements IToolInitializer
{
    public void initialize(ToolPlugin plugin)
    {
        com.arjuna.ats.internal.jta.Implementations.initialise();   // needed for XAResourceRecord

        try
        {
            Class<?> c1 = Class.forName("com.arjuna.ats.internal.jts.Implementations");
            Class<?> c2 = Class.forName("com.arjuna.ats.internal.jta.Implementationsx"); // needed for XAResourceRecord

            c1.getMethod("initialise").invoke(null);
            c2.getMethod("initialise").invoke(null);
        }
        catch (Exception e)
        {
            // not JTS
        }

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
        });     }
}
