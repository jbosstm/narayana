package io.narayana.lra;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RequestBuilder {
    private HttpRequestBase httpRequest;
    private ArrayList<String> paths;

    private URIBuilder builder;
    private boolean async;
    private long timeout;
    private TimeUnit timeUnit;

    public RequestBuilder(URI baseURI) {
        builder = new URIBuilder(baseURI);
        paths = new ArrayList<>();

        Collections.addAll(paths, strip(baseURI.getPath()).split("/"));
    }

    private String strip(String path) {
        return path.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    public RequestBuilder path(String path) {
        Collections.addAll(paths, strip(path).split("/"));

        return this;
    }

    public RequestBuilder queryParam(String name, long value) {
        builder.setParameter(name, Long.toString(value));
        return this;
    }

    public RequestBuilder queryParam(String name, String value) {
        builder.setParameter(name, value);
        return this;
    }

    public RequestBuilder queryParam(String name, boolean value) {
        builder.setParameter(name, Boolean.toString(value));
        return this;
    }

    /**
     * create a request. This method must be called before adding
     * request headers
     * @return the builder
     */
    public RequestBuilder request() {
        builder.setPathSegments(paths);

        httpRequest = new HttpGet();
        return this;
    }

    /**
     * add a request header. Must be called after calling {@link RequestBuilder#request()}
     * @param name the name of the header
     * @param value the header value
     * @return the builder
     */
    public RequestBuilder header(String name, String value) {
        httpRequest.addHeader(name, value);
        return this;
    }

    /**
     * add a request header. Must be called after calling {@link RequestBuilder#request()}
     * @param name the name of the header
     * @param value the header value
     * @return the builder
     */
    public RequestBuilder header(String name, URI value) {
        httpRequest.addHeader(name, value == null ? null : value.toASCIIString());
        return this;
    }

    public RequestBuilder async(long timeout, TimeUnit timeUnit) {
        this.async = true;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        return this;
    }

    private HttpRequestBase setContent(HttpEntityEnclosingRequestBase req, String content, String mediaType) {
        if (content != null) {
            if (!mediaType.equals(MediaType.TEXT_PLAIN)) {
                throw new IllegalArgumentException("RequestBuilder: the only supported content type is " + mediaType);
            }
            req.setEntity(new StringEntity(content, HTTP.DEF_CONTENT_CHARSET));
        }

        return req;
    }

    private URI build() {
        try {
            return builder.build();
        } catch (URISyntaxException e) {
            throw new WebApplicationException(e);
        }
    }

    public ResponseHolder put() throws WebApplicationException {
        return send(setContent(new HttpPut(build()), "", ContentType.TEXT_PLAIN.getMimeType()));
    }

    public ResponseHolder put(String content, String mediaType) throws WebApplicationException {
        return send(setContent(new HttpPut(build()), content, mediaType));
    }

    public ResponseHolder post() throws WebApplicationException {
        return send(setContent(new HttpPost(build()), "", ContentType.TEXT_PLAIN.getMimeType()));
    }

    public ResponseHolder get() throws WebApplicationException {
        try {
            return send(new HttpGet(builder.build()));
        } catch (URISyntaxException e) {
            throw new WebApplicationException(e);
        }
    }

    public ResponseHolder delete() throws WebApplicationException {
        try {
            return send(new HttpDelete(builder.build()));
        } catch (URISyntaxException e) {
            throw new WebApplicationException(e);
        }
    }

    private ResponseHolder send(HttpRequestBase req) throws WebApplicationException {
        for (Header header : httpRequest.getAllHeaders()) {
            req.addHeader(header);
        }

        LRAHttpClient httpClient = LRAHttpClient.getClient();

        try {
            return async ? httpClient.request(req, timeout, timeUnit) : httpClient.request(req);
        } catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
            throw new WebApplicationException(e);
        }
    }
}
