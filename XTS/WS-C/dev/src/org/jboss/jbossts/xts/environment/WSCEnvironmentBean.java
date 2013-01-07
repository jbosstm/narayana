package org.jboss.jbossts.xts.environment;

import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

import java.net.InetAddress;

/**
 * bean storing WS-C implementation configuration values derived from the xts properties file, system property
 * settings and, in case we are running inside JBossAS the xts bean.xml file
 */
@PropertyPrefix(prefix = "org.jboss.jbossts.xts")
public class WSCEnvironmentBean
{
    /**
     * the initial time to wait before resending a coordination protocol messaage. this is increased
     * gradually, doubling every two resends up to the maximum  value
     */
    @FullPropertyName(name="org.jboss.jbossts.xts.transport.initialTransportPeriod")
    private volatile int initialTransportPeriod = 5 * 1000;

    /**
     * the maximum time to wait before resending a coordination protocol messaage
     */
    @FullPropertyName(name="org.jboss.jbossts.xts.transport.maximumTransportPeriod")
    private volatile int maximumTransportPeriod = 300 * 1000;

    /**
     * the wait period after sending a  protocol message and not getting the expected reply before it is
     * decided that the server at the other end has crashed. this wait is only used for some exchanges.
     * a timeout may be handled by transitioning to a different transaction state. alternatively the service
     * may merely begin resending the message starting at the initial transport  period.
     */
    @FullPropertyName(name="org.jboss.jbossts.xts.transport.transportTimeout")
    private volatile int transportTimeout =  30 * 1000;

    /**
     * the bind address on which the web service is listening. when running in JBoss AS this is derived from
     * the JBoss Web service bean by injection.
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.bind.address")
    private volatile String bindAddress11 = "localhost";

    /**
     * the port which will be used to service non-secure requests.
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.bind.port")
    private volatile int bindPort11 = 8080;

    /**
     * the port which will be used to service secure requests.
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.bind.port.secure")
    private volatile int bindPortSecure11 = 8443;

    /**
     * the URL to be used by XTS clients to address the Activation Coordinator service when starting a transaction.
     * if this is undefined then a URL is constructed using the scheme, host address, port and URL path specified
     * in the properties file or, where they are missing, using their default values.
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.coordinatorURL")
    private volatile String coordinatorURL11 = null;

    /**
     * the scheme which will be used to construct a coordinator URL if it is not specified
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.coordinator.scheme")
    private volatile String coordinatorScheme11 = "http";

    /**
     * the address which will be used to construct a coordinator URL if it is not specified
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.coordinator.address")
    private volatile String coordinatorAddress11 = null;

    /**
     * the port which will be used to construct a coordinator URL if it is not specified
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.coordinator.port")
    private volatile int coordinatorPort11 = 8080;

    /**
     * the path which will be used to construct a coordinator URL if it is not specified
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts11.coordinator.path")
    private volatile String coordinatorPath11 = "ws-c11/ActivationService";

    /**
     * the bind address on which the web service is listening. when running in JBoss AS this is derived from
     * the JBoss Web service bean by injection. if not set it defaults to localhost.
     */
    @Deprecated
    @FullPropertyName(name = "org.jboss.jbossts.xts.bind.address")
    private volatile String bindAddress10 = "localhost";

    /**
     * the port which will be used to service non-secure requests. if not set it defaults to 8080.
     */
    @Deprecated
    @FullPropertyName(name = "org.jboss.jbossts.xts.bind.port")
    private volatile int bindPort10 = 8080;
    
    /**
     * the port which will be used to service secure requests. if not set it defaults to 8080. n.b. the
     * 1.0 implementation deploys its own servlet and it only listens on the non-secure  port.
     */
    @Deprecated
    @FullPropertyName(name = "org.jboss.jbossts.xts.bind.port.secure")
    private volatile int bindPortSecure10 = 8080;

    /**
     * the URL to be used by XTS clients to address the Activation Coordinator service when starting a transaction.
     * if this is undefined then a URL is constructed using the scheme, host address, port and URL path specified
     * in the properties file or, where they are missing, using their default values.
     */
    @Deprecated
    @FullPropertyName(name = "org.jboss.jbossts.xts.coordinatorYRL")
    private volatile String coordinatorURL10 = null;

    /**
     * the scheme which will be used to construct a coordinator URL if it is not specified
     */
    @Deprecated
    @FullPropertyName(name = "org.jboss.jbossts.xts.coordinator.scheme")
    private volatile String coordinatorScheme10 = "http";

    /**
     * the address which will be used to construct a coordinator URL if it is not specified
     */
    @Deprecated
    @FullPropertyName(name = "org.jboss.jbossts.xts.coordinator.address")
    private volatile String coordinatorAddress10 = null;

    /**
     * the port which will be used to construct a coordinator URL if it is not specified
     */
    @Deprecated
    @FullPropertyName(name = "org.jboss.jbossts.xts.coordinator.port")
    private volatile int coordinatorPort10 = 8080;

    /**
     * the path which will be used to construct a coordinator URL if it is not specified
     */
    @Deprecated
    @FullPropertyName(name = "org.jboss.jbossts.xts.coordinator.path")
    private volatile String coordinatorPath10 = "ws-c10/soap/ActivationCoordinator";

    /**
     * the URL path component of the URL at which 1.1 WS-C services have been mapped. if this is null
     * then the path is defaulted to "ws-c11"
     */
    @FullPropertyName(name = "org.jboss.jbossts.xts.wsc11.serviceURLPath")
    private volatile String serviceURLPath = null;

    /**
     * Returns initial time to wait before resending a coordination protocol message.
     * This is increased gradually, doubling every two resends up to the maximum value
     *
     * @return initial time to wait before resending a coordination protocol message.
     */
    public int getInitialTransportPeriod() {
        return initialTransportPeriod;
    }

    /**
     * Sets initial time to wait before resending a coordination protocol message.
     *
     * @param initialTransportPeriod initial time to wait before resending a coordination protocol message.
     */
    public void setInitialTransportPeriod(int initialTransportPeriod) {
        this.initialTransportPeriod = initialTransportPeriod;
    }

    /**
     * Returns maximum time to wait before resending a coordination protocol message.
     *
     * @return maximum time to wait before resending a coordination protocol message.
     */
    public int getMaximumTransportPeriod() {
        return maximumTransportPeriod;
    }

    /**
     * Sets maximum time to wait before resending a coordination protocol message.
     *
     * @param maximumTransportPeriod maximum time to wait before resending a coordination protocol message.
     */
    public void setMaximumTransportPeriod(int maximumTransportPeriod) {
        this.maximumTransportPeriod = maximumTransportPeriod;
    }

    /**
     * Returns the wait period after sending a protocol message and not getting the expected reply before it is decided that the server at the other end has crashed.
     * This wait is only used for some exchanges. A timeout may be handled by transitioning to a different transaction state.
     * Alternatively the service may merely begin resending the message starting at the initial transport period.
     *
     * @return wait period before deciding that the server at the other end has crashed.
     */
    public int getTransportTimeout() {
        return transportTimeout;
    }

    /**
     * Sets the wait period after sending a protocol message and not getting the expected reply before it is decided that the server at the other end has crashed.
     *
     * @param transportTimeout wait period before deciding that the server at the other end has crashed.
     */
    public void setTransportTimeout(int transportTimeout) {
        this.transportTimeout = transportTimeout;
    }

    /**
     * this setter is used by the microcontainer to inject the inet address supplied during app server startup.
     */
    public void setHttpBindInetAddress(InetAddress httpBindInetAddress) {
        this.bindAddress11 = httpBindInetAddress.getHostAddress();
        this.bindAddress10 = this.bindAddress11;
    }

    /**
     * Returns the bind address on which the web service is listening.
     * When running in JBoss AS this is derived from the JBoss Web service bean by injection.
     *
     * WS-C version: 1.1.
     *
     * @return the bind address on which the web service is listening.
     */
    public String getBindAddress11() {
        return bindAddress11;
    }

    /**
     * Sets the bind address on which the web service is listening.
     *
     * WS-C version: 1.1.
     *
     * @param bindAddress11 the bind address on which the web service is listening.
     */
    public void setBindAddress11(String bindAddress11) {
        this.bindAddress11 = bindAddress11;
    }

    /**
     * Returns the port which will be used to service non-secure requests.
     *
     * WS-C version: 1.1.
     *
     * @return the port which will be used to service non-secure requests.
     */
    public int getBindPort11() {
        return bindPort11;
    }

    /**
     * Sets the port which will be used to service non-secure requests.
     *
     * WS-C version: 1.1.
     *
     * @param bindPort11 the port which will be used to service non-secure requests.
     */
    public void setBindPort11(int bindPort11) {
        if (bindPort11 > 0) {
            this.bindPort11 = bindPort11;
        }
    }

    /**
     * Returns the port which will be used to service secure requests.
     *
     * WS-C version: 1.1.
     *
     * @return the port which will be used to service secure requests.
     */
    public int getBindPortSecure11() {
        return bindPortSecure11;
    }

    /**
     * Sets the port which will be used to service secure requests.
     *
     * WS-C version: 1.1.
     *
     * @param bindPortSecure11 the port which will be used to service secure requests.
     */
    public void setBindPortSecure11(int bindPortSecure11) {
        if (bindPortSecure11 > 0) {
            this.bindPortSecure11 = bindPortSecure11;
        }
    }

    /**
     * Returns the URL to be used by XTS clients to address the Activation Coordinator service when starting a transaction.
     * If this is undefined then a URL is constructed using the scheme, host address, port and URL path specified in the properties file or,
     * where they are missing, using their default values.
     *
     * WS-C version: 1.1.
     *
     * @return the URL to be used by XTS clients to address the Activation Coordinator service when starting a transaction.
     */
    public String getCoordinatorURL11() {
        if (coordinatorURL11 == null) {
            // compute the required value
            return getCoordinatorScheme11() + "://" + getCoordinatorAddress11() + ":" + getCoordinatorPort11() + "/" + getCoordinatorPath11();
        }
        return coordinatorURL11;
    }

    /**
     * Sets the URL to be used by XTS clients to address the Activation Coordinator service when starting a transaction.
     *
     * WS-C version: 1.1.
     *
     * @param coordinatorURL11 the URL to be used by XTS clients to address the Activation Coordinator service when starting a transaction.
     */
    public void setCoordinatorURL11(String coordinatorURL11) {
        this.coordinatorURL11 = coordinatorURL11;
    }

    /**
     * Returns the scheme which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.1.
     *
     * @return the scheme which will be used to construct a coordinator URL if it is not specified.
     */
    public String getCoordinatorScheme11() {
        return coordinatorScheme11;
    }

    /**
     * Sets the scheme which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.1.
     *
     * @param coordinatorScheme11 the scheme which will be used to construct a coordinator URL if it is not specified.
     */
    public void setCoordinatorScheme11(String coordinatorScheme11) {
        this.coordinatorScheme11 = coordinatorScheme11;
    }

    /**
     * Returns the address which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.1.
     *
     * @return the address which will be used to construct a coordinator URL if it is not specified.
     */
    public String getCoordinatorAddress11() {
        if (coordinatorAddress11 == null) {
            return bindAddress11;
        }
        return coordinatorAddress11;
    }

    /**
     * Sets the address which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.1.
     *
     * @param coordinatorAddress11 the address which will be used to construct a coordinator URL if it is not specified.
     */
    public void setCoordinatorAddress11(String coordinatorAddress11) {
        this.coordinatorAddress11 = coordinatorAddress11;
    }

    /**
     * Returns the port which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.1.
     *
     * @return the port which will be used to construct a coordinator URL if it is not specified.
     */
    public int getCoordinatorPort11() {
        if (coordinatorPort11 == 0) {
            // be sure to use the port appropriate to the protocol
            String scheme = getCoordinatorScheme11();
            if ("https".equals(scheme)) {
                return bindPortSecure11;
            } else {
                return bindPort11;
            }
        }
        return coordinatorPort11;
    }

    /**
     * Sets the port which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.1.
     *
     * @param coordinatorPort11 the port which will be used to construct a coordinator URL if it is not specified.
     */
    public void setCoordinatorPort11(int coordinatorPort11) {
        this.coordinatorPort11 = coordinatorPort11;
    }

    /**
     * Returns the path which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.1.
     *
     * @return the path which will be used to construct a coordinator URL if it is not specified
     */
    public String getCoordinatorPath11() {
        return coordinatorPath11;
    }

    /**
     * Sets the path which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.1.
     *
     * @param coordinatorPath11 the path which will be used to construct a coordinator URL if it is not specified.
     */
    public void setCoordinatorPath11(String coordinatorPath11) {
        this.coordinatorPath11 = coordinatorPath11;
    }

    /**
     * Returns the bind address on which the web service is listening.
     * When running in JBoss AS this is derived from the JBoss Web service bean by injection.
     * If not set it defaults to localhost.
     *
     * WS-C version: 1.0.
     *
     * @return the bind address on which the web service is listening.
     */
    @Deprecated
    public String getBindAddress10() {
        return bindAddress10;
    }

    /**
     * Sets the bind address on which the web service is listening.
     *
     * WS-C version: 1.0.
     *
     * @param bindAddress10 the bind address on which the web service is listening.
     */
    @Deprecated
    public void setBindAddress10(String bindAddress10) {
        this.bindAddress10 = bindAddress10;
    }

    /**
     * Returns the port which will be used to service non-secure requests.
     * If not set it defaults to 8080.
     *
     * WS-C version: 1.0.
     *
     * @return the port which will be used to service non-secure requests.
     */
    @Deprecated
    public int getBindPort10() {
        return bindPort10;
    }

    /**
     * Sets the port which will be used to service non-secure requests.
     *
     * WS-C version: 1.0.
     *
     * @param bindPort10 the port which will be used to service non-secure requests.
     */
    @Deprecated
    public void setBindPort10(int bindPort10) {
        if (bindPort10 > 0) {
            this.bindPort10 = bindPort10;
        }
    }

    /**
     * Returns the port which will be used to service secure requests.
     * If not set it defaults to 8080.
     * n.b. the 1.0 implementation deploys its own servlet and it only listens on the non-secure port.
     *
     * WS-C version: 1.0.
     *
     * @return the port which will be used to service secure requests.
     */
    @Deprecated
    public int getBindPortSecure10() {
        return bindPortSecure10;
    }

    /**
     * Sets the port which will be used to service secure requests.
     * n.b. the 1.0 implementation deploys its own servlet and it only listens on the non-secure port.
     *
     * WS-C version: 1.0.
     *
     * @param bindPortSecure10 the port which will be used to service secure requests.
     */
    @Deprecated
    public void setBindPortSecure10(int bindPortSecure10) {
        if (bindPortSecure10 > 0) {
            this.bindPortSecure10 = bindPortSecure10;
        }
    }

    /**
     * Returns the URL to be used by XTS clients to address the Activation Coordinator service when starting a transaction.
     * If this is undefined then a URL is constructed using the scheme, host address, port and URL path specified in the properties file or,
     * where they are missing, using their default values.
     *
     * WS-C version: 1.0.
     *
     * @return the URL to be used by XTS clients to address the Activation Coordinator service when starting a transaction.
     */
    @Deprecated
    public String getCoordinatorURL10() {
        if (coordinatorURL10 == null) {
            // compute the required value
            return getCoordinatorScheme10() + "://" + getCoordinatorAddress10() + ":" + getCoordinatorPort10() + "/" + getCoordinatorPath10();
        }
        return coordinatorURL10;
    }

    /**
     * Sets the URL to be used by XTS clients to address the Activation Coordinator service when starting a transaction.
     *
     * WS-C version: 1.0.
     *
     * @param coordinatorURL10 the URL to be used by XTS clients to address the Activation Coordinator service when starting a transaction.
     */
    @Deprecated
    public void setCoordinatorURL10(String coordinatorURL10) {
        this.coordinatorURL10 = coordinatorURL10;
    }

    /**
     * Returns the scheme which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.0.
     *
     * @return the scheme which will be used to construct a coordinator URL if it is not specified.
     */
    @Deprecated
    public String getCoordinatorScheme10() {
        return coordinatorScheme10;
    }

    /**
     * Sets the scheme which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.0.
     *
     * @param coordinatorScheme10 the scheme which will be used to construct a coordinator URL if it is not specified.
     */
    @Deprecated
    public void setCoordinatorScheme10(String coordinatorScheme10) {
        this.coordinatorScheme10 = coordinatorScheme10;
    }

    /**
     * Returns the address which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.0.
     *
     * @return the address which will be used to construct a coordinator URL if it is not specified.
     */
    @Deprecated
    public String getCoordinatorAddress10() {
        if (coordinatorAddress10 == null) {
            return bindAddress10;
        }
        return coordinatorAddress10;
    }

    /**
     * Sets the address which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.0.
     *
     * @param coordinatorAddress10 the address which will be used to construct a coordinator URL if it is not specified.
     */
    @Deprecated
    public void setCoordinatorAddress10(String coordinatorAddress10) {
        this.coordinatorAddress10 = coordinatorAddress10;
    }

    /**
     * Returns the port which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.0.
     *
     * @return the port which will be used to construct a coordinator URL if it is not specified.
     */
    @Deprecated
    public int getCoordinatorPort10() {
        if (coordinatorPort10 == 0) {
            // be sure to use the port appropriate to the protocol
            String scheme = getCoordinatorScheme10();
            if ("https".equals(scheme)) {
                return bindPortSecure10;
            } else {
                return bindPort10;
            }
        }
        return coordinatorPort10;
    }

    /**
     * Sets the port which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.0.
     *
     * @param coordinatorPort10 the port which will be used to construct a coordinator URL if it is not specified.
     */
    @Deprecated
    public void setCoordinatorPort10(int coordinatorPort10) {
        this.coordinatorPort10 = coordinatorPort10;
    }

    /**
     * Returns the path which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.0.
     *
     * @return the path which will be used to construct a coordinator URL if it is not specified.
     */
    @Deprecated
    public String getCoordinatorPath10() {
        return coordinatorPath10;
    }

    /**
     * Sets the path which will be used to construct a coordinator URL if it is not specified.
     *
     * WS-C version: 1.0.
     *
     * @param coordinatorPath10 the path which will be used to construct a coordinator URL if it is not specified.
     */
    @Deprecated
    public void setCoordinatorPath10(String coordinatorPath10) {
        this.coordinatorPath10 = coordinatorPath10;
    }

    /**
     * Returns the URL path component of the URL at which 1.1 WS-C services have been mapped.
     * If this is null then the path is defaulted to "ws-c11".
     *
     * @return the URL path component of the URL at which 1.1 WS-C services have been mapped.
     */
    public String getServiceURLPath() {
        return serviceURLPath;
    }

    /**
     * Sets the URL path component of the URL at which 1.1 WS-C services have been mapped.
     *
     * @param serviceURLPath the URL path component of the URL at which 1.1 WS-C services have been mapped.
     */
    public void setServiceURLPath(String serviceURLPath) {
        this.serviceURLPath = serviceURLPath;
    }
}
