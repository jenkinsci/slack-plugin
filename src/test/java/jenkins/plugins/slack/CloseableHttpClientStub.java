package jenkins.plugins.slack;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

public class CloseableHttpClientStub extends CloseableHttpClient {

    private int numberOfCallsToExecuteMethod;
    private int httpStatus;
    private boolean failAlternateResponses = false;

    public CloseableHttpResponse execute(HttpUriRequest post) {
        numberOfCallsToExecuteMethod++;
        if (failAlternateResponses && (numberOfCallsToExecuteMethod % 2 == 0)) {
            return new CloseableHttpResponseStub(HttpStatus.SC_NOT_FOUND);
        } else {
            return new CloseableHttpResponseStub(httpStatus);
        }
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext) {
        return null;
    }

    public int getNumberOfCallsToExecuteMethod() {
        return numberOfCallsToExecuteMethod;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public void setFailAlternateResponses(boolean failAlternateResponses) {
        this.failAlternateResponses = failAlternateResponses;
    }
}
