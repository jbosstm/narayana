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
    @Deprecated
    @FullPropertyName(name = "org.jboss.jbossts.xts.wsat.UserTransaction")
    private volatile String userTransaction10 = "com.arjuna.mwlabs.wst.at.remote.UserTransactionImple";

    /**
     * the name of the class used to implement the WSAT 1.0 TransactionManager API.
     */
    @Deprecated
    @FullPropertyName(name = "org.jboss.jbossts.xts.wsa1.TransactionManager")
    private volatile String transactionManager10 = "com.arjuna.mwlabs.wst.at.remote.TransactionManagerImple";

    /**
     * the name of the class used to implement the WSBA 1.0 UserBusinessActivity API.
     */
    @Deprecated
    @FullPropertyName(name = "org.jboss.jbossts.xts.wsba.UserBusinessActivity")
    private volatile String userBusinessActivity10 = "com.arjuna.mwlabs.wst.ba.remote.UserBusinessActivityImple";

    /**
     * the name of the class used to implement the WSBA 1.0 BusinessActivityManager API.
     */
    @Deprecated
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

    /**
     * Returns the name of the class used to implement the WSAT 1.1 UserTransaction API.
     *
     * @return the name of the class used to implement the WSAT 1.1 UserTransaction API.
     */
    public String getUserTransaction11() {
        return userTransaction11;
    }

    /**
     * Sets the name of the class used to implement the WSAT 1.1 UserTransaction API.
     *
     * @param userTransaction11 the name of the class used to implement the WSAT 1.1 UserTransaction API.
     */
    public void setUserTransaction11(String userTransaction11) {
        this.userTransaction11 = userTransaction11;
    }

    /**
     * Returns the name of the class used to implement the WSAT 1.1 TransactionManager API.
     *
     * @return the name of the class used to implement the WSAT 1.1 TransactionManager API.
     */
    public String getTransactionManager11() {
        return transactionManager11;
    }

    /**
     * Sets the name of the class used to implement the WSAT 1.1 TransactionManager API.
     *
     * @param transactionManager11 the name of the class used to implement the WSAT 1.1 TransactionManager API.
     */
    public void setTransactionManager11(String transactionManager11) {
        this.transactionManager11 = transactionManager11;
    }

    /**
     * Returns the name of the class used to implement the WSBA 1.1 UserBusinessActivity API.
     *
     * @return the name of the class used to implement the WSBA 1.1 UserBusinessActivity API.
     */
    public String getUserBusinessActivity11() {
        return userBusinessActivity11;
    }

    /**
     * Sets the name of the class used to implement the WSBA 1.1 UserBusinessActivity API.
     *
     * @param userBusinessActivity11 the name of the class used to implement the WSBA 1.1 UserBusinessActivity API.
     */
    public void setUserBusinessActivity11(String userBusinessActivity11) {
        this.userBusinessActivity11 = userBusinessActivity11;
    }

    /**
     * Returns the name of the class used to implement the WSBA 1.1 BusinessActivityManager API.
     *
     * @return the name of the class used to implement the WSBA 1.1 BusinessActivityManager API.
     */
    public String getBusinessActivityManager11() {
        return businessActivityManager11;
    }

    /**
     * Sets the name of the class used to implement the WSBA 1.1 BusinessActivityManager API.
     *
     * @param businessActivityManager11 the name of the class used to implement the WSBA 1.1 BusinessActivityManager API.
     */
    public void setBusinessActivityManager11(String businessActivityManager11) {
        this.businessActivityManager11 = businessActivityManager11;
    }

    /**
     * Returns the name of the class used to implement the WSAT 1.0 UserTransaction API.
     *
     * @return the name of the class used to implement the WSAT 1.0 UserTransaction API.
     */
    @Deprecated
    public String getUserTransaction10() {
        return userTransaction10;
    }

    /**
     * Sets the name of the class used to implement the WSAT 1.0 UserTransaction API.
     *
     * @param userTransaction10 the name of the class used to implement the WSAT 1.0 UserTransaction API.
     */
    @Deprecated
    public void setUserTransaction10(String userTransaction10) {
        this.userTransaction10 = userTransaction10;
    }

    /**
     * Returns the name of the class used to implement the WSAT 1.0 TransactionManager API.
     *
     * @return the name of the class used to implement the WSAT 1.0 TransactionManager API.
     */
    @Deprecated
    public String getTransactionManager10() {
        return transactionManager10;
    }

    /**
     * Sets the name of the class used to implement the WSAT 1.0 TransactionManager API.
     *
     * @param transactionManager10 the name of the class used to implement the WSAT 1.0 TransactionManager API.
     */
    @Deprecated
    public void setTransactionManager10(String transactionManager10) {
        this.transactionManager10 = transactionManager10;
    }

    /**
     * Returns the name of the class used to implement the WSBA 1.0 UserBusinessActivity API.
     *
     * @return the name of the class used to implement the WSBA 1.0 UserBusinessActivity API.
     */
    @Deprecated
    public String getUserBusinessActivity10() {
        return userBusinessActivity10;
    }

    /**
     * Sets the name of the class used to implement the WSBA 1.0 UserBusinessActivity API.
     *
     * @param userBusinessActivity10 the name of the class used to implement the WSBA 1.0 UserBusinessActivity API.
     */
    @Deprecated
    public void setUserBusinessActivity10(String userBusinessActivity10) {
        this.userBusinessActivity10 = userBusinessActivity10;
    }

    /**
     * Returns the name of the class used to implement the WSBA 1.0 BusinessActivityManager API.
     *
     * @return the name of the class used to implement the WSBA 1.0 BusinessActivityManager API.
     */
    @Deprecated
    public String getBusinessActivityManager10() {
        return businessActivityManager10;
    }

    /**
     * Sets the name of the class used to implement the WSBA 1.0 BusinessActivityManager API.
     *
     * @param businessActivityManager10 the name of the class used to implement the WSBA 1.0 BusinessActivityManager API.
     */
    @Deprecated
    public void setBusinessActivityManager10(String businessActivityManager10) {
        this.businessActivityManager10 = businessActivityManager10;
    }

    /**
     * Returns the URL path component of the URL at which 1.1 WS-T coordinator services have been mapped.
     * If this is null then the path is defaulted to "ws-t11-coordinator".
     *
     * @return the URL path component of the URL at which 1.1 WS-T coordinator services have been mapped.
     */
    public String getCoordinatorServiceURLPath() {
        return coordinatorServiceURLPath;
    }

    /**
     * Sets the URL path component of the URL at which 1.1 WS-T coordinator services have been mapped.
     *
     * @param coordinatorServiceURLPath the URL path component of the URL at which 1.1 WS-T coordinator services have been mapped.
     */
    public void setCoordinatorServiceURLPath(String coordinatorServiceURLPath) {
        this.coordinatorServiceURLPath = coordinatorServiceURLPath;
    }

    /**
     * Returns the URL path component of the URL at which 1.1 WS-T client services have been mapped.
     * If this is null then the path is defaulted to "ws-t11-client".
     *
     * @return the URL path component of the URL at which 1.1 WS-T client services have been mapped.
     */
    public String getClientServiceURLPath() {
        return clientServiceURLPath;
    }

    /**
     * Sets the URL path component of the URL at which 1.1 WS-T client services have been mapped.
     *
     * @param clientServiceURLPath the URL path component of the URL at which 1.1 WS-T client services have been mapped.
     */
    public void setClientServiceURLPath(String clientServiceURLPath) {
        this.clientServiceURLPath = clientServiceURLPath;
    }

    /**
     * Returns the URL path component of the URL at which 1.1 WS-T participant services have been mapped.
     * If this is null then the path is defaulted to "ws-t11-participant".
     *
     * @return the URL path component of the URL at which 1.1 WS-T participant services have been mapped.
     */
    public String getParticipantServiceURLPath() {
        return participantServiceURLPath;
    }

    /**
     * Sets the URL path component of the URL at which 1.1 WS-T participant services have been mapped.
     *
     * @param participantServiceURLPath the URL path component of the URL at which 1.1 WS-T participant services have been mapped.
     */
    public void setParticipantServiceURLPath(String participantServiceURLPath) {
        this.participantServiceURLPath = participantServiceURLPath;
    }
}
