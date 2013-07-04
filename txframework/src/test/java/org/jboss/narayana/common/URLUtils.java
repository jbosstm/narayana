package org.jboss.narayana.common;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public final class URLUtils {

    public String getBaseUrl() {
        String baseAddress = System.getProperty("jboss.bind.address");

        if (baseAddress == null) {
            baseAddress = "http://localhost";
        } else if (!baseAddress.toLowerCase().startsWith("http://") && !baseAddress.toLowerCase().startsWith("https://")) {
            baseAddress = "http://" + baseAddress;
        }

        if (baseAddress.endsWith("/")) {
            baseAddress = baseAddress.substring(0, baseAddress.length() - 1);
        }

        return baseAddress;
    }

    public String getBasePort() {
        final String basePort = System.getProperty("jboss.bind.port");

        if (basePort == null) {
            return "8080";
        }

        return basePort;
    }

}
