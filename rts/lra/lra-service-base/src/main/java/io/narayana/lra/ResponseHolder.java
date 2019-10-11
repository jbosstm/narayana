package io.narayana.lra;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ResponseHolder {
    private HttpRequestBase httpRequest;
    private HttpResponse httpResponse;
    private int status;
    private String locationHeader;
    private String responseString;

    public ResponseHolder(HttpRequestBase httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        status = httpResponse.getStatusLine().getStatusCode();
        Header h = httpResponse.getFirstHeader(HttpHeaders.LOCATION);
        locationHeader = h == null ? null : h.getValue();
        HttpEntity entity = httpResponse.getEntity();
        try {
            responseString = entity != null ? EntityUtils.toString(entity, "UTF-8") : null;
        } catch (IOException e) {
            responseString = null;
        }
    }

    public URI getRequestURI() {
        return httpRequest.getURI();
    }

    public int getStatus() {
        return status;
    }

    public String getLocationHeader() {
        return locationHeader;
    }

    public String getResponseString() {
        return responseString;
    }

    public URI getLocationHeaderAsURI() throws URISyntaxException {
        return locationHeader != null ? new URI(locationHeader) : null;
    }

    public boolean hasEntity() {
        return responseString != null;
    }

    public String getLastHeader(String headerName) {
        return httpResponse.getLastHeader(headerName).getValue();
    }

    public String readEntity() {
        return responseString;
    }

    public String getHeader(String name) {
        Header header = httpResponse.getLastHeader(name);

        return header == null ? null : header.getValue();
    }
}
