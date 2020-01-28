package io.narayana.lra;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.HttpRequestFutureTask;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LRAHttpClient {
    public static final long PARTICIPANT_TIMEOUT = 2; // number of seconds to wait for requests

    private static LRAHttpClient client = new LRAHttpClient();

    private FutureRequestExecutionService futureRequestExecutionService;

    private LRAHttpClient() {
        HttpClient httpClient = HttpClientBuilder.create().setMaxConnPerRoute(5).build();
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        futureRequestExecutionService = new FutureRequestExecutionService(httpClient, executorService);
    }

    public static LRAHttpClient getClient() {
        return client;
    }

    ResponseHolder request(HttpRequestBase httpMethod, long timelimit, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        HttpRequestFutureTask<ResponseHolder> futureTask = submitFutureTask(httpMethod);

        // if any user wants this method to be async return the future.
        // For now the current users are okay to block (but not indefinitely).
        return futureTask.get(timelimit, unit);
    }

    public ResponseHolder request(HttpRequestBase httpMethod) throws IOException {
        return submitTask(httpMethod);
    }

    private ResponseHolder submitTask(HttpRequestBase httpMethod) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();

        CloseableHttpResponse httpResponse = client.execute(httpMethod);

        return new ResponseHolder(httpMethod, httpResponse);
    }

    private HttpRequestFutureTask<ResponseHolder> submitFutureTask(HttpRequestBase httpMethod) {//throws InterruptedException, ExecutionException {
        ResponseHandler<ResponseHolder> responseHandler = httpResponse -> new ResponseHolder(httpMethod, httpResponse);

        return futureRequestExecutionService.execute(
                httpMethod, HttpClientContext.create(),
                responseHandler);
    }
}
