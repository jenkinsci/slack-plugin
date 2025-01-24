package org.apache.hc.client5.http.impl.classic;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;

public class CloseableHttpClientStub extends CloseableHttpClient {

    private int numberOfCallsToExecuteMethod;
    private int httpStatus;
    private boolean failAlternateResponses = false;
    private ClassicHttpRequest lastRequest = null;
    private CloseableHttpResponse httpResponse = null;


    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, ClassicHttpRequest request, HttpContext context) {
        lastRequest = request;
        numberOfCallsToExecuteMethod++;
        if (httpResponse != null) {
            return httpResponse;
        } else if (failAlternateResponses && (numberOfCallsToExecuteMethod % 2 == 0)) {
            return new CloseableHttpResponseStub(HttpStatus.SC_NOT_FOUND).toCloseableHttpResponse();
        } else {
            return new CloseableHttpResponseStub(httpStatus).toCloseableHttpResponse();
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void close(CloseMode closeMode) {

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

    public ClassicHttpRequest getLastRequest() {
        return lastRequest;
    }

}
