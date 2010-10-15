package org.jboss.jbossts.xts.environment;

import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

/**
 * bean storing WS-T 1.0 implementation configuration values derived from the xts properties file, system property
 * settings and, in case we are running inside JBossAS the xts bean.xml file
 */
@PropertyPrefix(prefix = "org.jboss.jbossts.xts")
public class WSTEnvironmentBean
{
    /**
     * the name of the class used to implement the WSAT 1.1 UserTransaction API.
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.wsat.UserTransaction")
    private volatile String userTransaction11 = "com.arjuna.mwlabs.wst11.at.remote.UserTransactionImple";

    /**
     * the name of the class used to implement the WSAT 1.1 TransactionManager API.
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.wsat.TransactionManager")
    private volatile String transactionManager11 = "com.arjuna.mwlabs.wst11.at.remote.TransactionManagerImple";

    /**
     * the name of the class used to implement the WSBA 1.1 UserBusinessActivity API.
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.wsba.UserBusinessActivity")
    private volatile String userBusinessActivity11 = "com.arjuna.mwlabs.wst11.ba.remote.UserBusinessActivityImple";

    /**
     * the name of the class used to implement the WSBA 1.1 BusinessActivityManager API.
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.wsba.BusinessActivityManager")
    private volatile String businessActivityManager11 = "com.arjuna.mwlabs.wst11.ba.remote.BusinessActivityManagerImple";
    /**
     * the name of the class used to implement the WSAT 1.0 UserTransaction API.
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts.wsat.UserTransaction")
    private volatile String userTransaction10 = "com.arjuna.mwlabs.wst.at.remote.UserTransactionImple";

    /**
     * the name of the class used to implement the WSAT 1.0 TransactionManager API.
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts.wsa1.TransactionManager")
    private volatile String transactionManager10 = "com.arjuna.mwlabs.wst.at.remote.TransactionManagerImple";

    /**
     * the name of the class used to implement the WSBA 1.0 UserBusinessActivity API.
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts.wsba.UserBusinessActivity")
    private volatile String userBusinessActivity10 = "com.arjuna.mwlabs.wst.ba.remote.UserBusinessActivityImple";

    /**
     * the name of the class used to implement the WSBA 1.0 BusinessActivityManager API.
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts.wsba.BusinessActivityManager")
    private volatile String businessActivityManager10 = "com.arjuna.mwlabs.wst.ba.remote.BusinessActivityManagerImple";

    /**
     * the URL path component of the URL at which 1.1 WS-T coordinator services have been mapped. if this is null
     * then the path is defaulted to "ws-t11-coordinator"
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.wst.coordinatorServiceURLPath")
    private volatile String coordinatorServiceURLPath = null;

    /**
     * the URL path component of the URL at which 1.1 WS-T client services have been mapped. if this is null
     * then the path is defaulted to "ws-t11-client"
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.wst.clientServiceURLPath")
    private volatile String clientServiceURLPath = null;

    /**
     * the URL path component of the URL at which 1.1 WS-T participant services have been mapped. if this is null
     * then the path is defaulted to "ws-t11-participant"
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.wst.participantServiceURLPath")
    private volatile String participantServiceURLPath = null;

    public String getUserTransaction11() {
        return userTransaction11;
    }

    public void setUserTransaction11(String userTransaction11) {
        this.userTransaction11 = userTransaction11;
    }

    public String getTransactionManager11() {
        return transactionManager11;
    }

    public void setTransactionManager11(String transactionManager11) {
        this.transactionManager11 = transactionManager11;
    }

    public String getUserBusinessActivity11() {
        return userBusinessActivity11;
    }

    public void setUserBusinessActivity11(String userBusinessActivity11) {
        this.userBusinessActivity11 = userBusinessActivity11;
    }

    public String getBusinessActivityManager11() {
        return businessActivityManager11;
    }

    public void setBusinessActivityManager11(String businessActivityManager11) {
        this.businessActivityManager11 = businessActivityManager11;
    }

    public String getUserTransaction10() {
        return userTransaction10;
    }

    public void setUserTransaction10(String userTransaction10) {
        this.userTransaction10 = userTransaction10;
    }

    public String getTransactionManager10() {
        return transactionManager10;
    }

    public void setTransactionManager10(String transactionManager10) {
        this.transactionManager10 = transactionManager10;
    }

    public String getUserBusinessActivity10() {
        return userBusinessActivity10;
    }

    public void setUserBusinessActivity10(String userBusinessActivity10) {
        this.userBusinessActivity10 = userBusinessActivity10;
    }

    public String getBusinessActivityManager10() {
        return businessActivityManager10;
    }

    public void setBusinessActivityManager10(String businessActivityManager10) {
        this.businessActivityManager10 = businessActivityManager10;
    }

    public String getCoordinatorServiceURLPath() {
        return coordinatorServiceURLPath;
    }

    public void setCoordinatorServiceURLPath(String coordinatorServiceURLPath) {
        this.coordinatorServiceURLPath = coordinatorServiceURLPath;
    }

    public String getClientServiceURLPath() {
        return clientServiceURLPath;
    }

    public void setClientServiceURLPath(String clientServiceURLPath) {
        this.clientServiceURLPath = clientServiceURLPath;
    }

    public String getParticipantServiceURLPath() {
        return participantServiceURLPath;
    }

    public void setParticipantServiceURLPath(String participantServiceURLPath) {
        this.participantServiceURLPath = participantServiceURLPath;
    }
}
