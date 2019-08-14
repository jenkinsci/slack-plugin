package jenkins.plugins.slack;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

public class CloseableHttpClientStub extends CloseableHttpClient {

    private int numberOfCallsToExecuteMethod;
    private int httpStatus;
    private boolean failAlternateResponses = false;
    private HttpUriRequest lastRequest = null;
    private CloseableHttpResponse httpResponse = null;

    @Override
    public CloseableHttpResponse execute(HttpUriRequest post) {
        lastRequest = post;
        numberOfCallsToExecuteMethod++;
        if (httpResponse != null) {
            return httpResponse;
        } else if (failAlternateResponses && (numberOfCallsToExecuteMethod % 2 == 0)) {
            return new CloseableHttpResponseStub(HttpStatus.SC_NOT_FOUND);
        } else {
            return new CloseableHttpResponseStub(httpStatus);
        }
    }

    @Override
    @SuppressWarnings("deprecation") //  deprecated abstract method
    public org.apache.http.conn.ClientConnectionManager getConnectionManager() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    @SuppressWarnings("deprecation") //  deprecated abstract method
    public org.apache.http.params.HttpParams getParams() {
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

    public void setHttpResponse(CloseableHttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public void setFailAlternateResponses(boolean failAlternateResponses) {
        this.failAlternateResponses = failAlternateResponses;
    }

    public HttpUriRequest getLastRequest() {
        return lastRequest;
    }
}
