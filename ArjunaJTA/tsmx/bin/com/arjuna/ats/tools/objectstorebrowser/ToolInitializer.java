package com.arjuna.ats.tools.objectstorebrowser;

import com.arjuna.ats.tools.toolsframework.plugin.ToolPlugin;
import com.arjuna.ats.tools.objectstorebrowser.rootprovider.InFlightTransactionPseudoStore;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.UidInfo;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.ats.jta.common.jtaPropertyManager;

import javax.transaction.Transaction;
import javax.transaction.xa.Xid;
import java.util.Map;

public class ToolInitializer implements IToolInitializer
{
    static String JTS_TM_CLASSNAME_STANDALONE =
            "com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple";
    static String JTS_TM_CLASSNAME_ATS =
            "com.arjuna.ats.jbossatx.jts.TransactionManagerDelegate";

    private boolean isJTS;

    public boolean isJTS() {
        return isJTS;
    }

    public void initialize(ToolPlugin plugin)
    {
        String tmClassName = jtaPropertyManager.getJTAEnvironmentBean().getTransactionManagerClassName();
        isJTS = (JTS_TM_CLASSNAME_STANDALONE.equals(tmClassName)
                || JTS_TM_CLASSNAME_ATS.equals(tmClassName));

        com.arjuna.ats.internal.jta.Implementations.initialise();   // needed for XAResourceRecord

        /* test whether we are using the JTS */
        if (isJTS)
        {
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
                if (xid instanceof XidImple) {
                    ((XidImple)xid).getTransactionUid();
                }

                return null;
            }
        });     }
}
