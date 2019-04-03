package jenkins.plugins.slack;

import java.util.Locale;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.params.HttpParams;

public class CloseableHttpResponseStub implements CloseableHttpResponse {

    private int responseCode;

    public CloseableHttpResponseStub(int response) {
        responseCode = response;
    }

    @Override
    public void close() {

    }

    @Override
    public StatusLine getStatusLine() {
        return new StatusLine() {
            @Override
            public ProtocolVersion getProtocolVersion() {
                return null;
            }

            @Override
            public int getStatusCode() {
                return responseCode;
            }

            @Override
            public String getReasonPhrase() {
                return null;
            }
        };
    }

    @Override
    public void setStatusLine(StatusLine statusLine) {

    }

    @Override
    public void setStatusLine(ProtocolVersion protocolVersion, int i) {

    }

    @Override
    public void setStatusLine(ProtocolVersion protocolVersion, int i, String s) {

    }

    @Override
    public void setStatusCode(int i) throws IllegalStateException {

    }

    @Override
    public void setReasonPhrase(String s) throws IllegalStateException {

    }

    @Override
    public HttpEntity getEntity() {
        return null;
    }

    @Override
    public void setEntity(HttpEntity httpEntity) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public void setLocale(Locale locale) {

    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return null;
    }

    @Override
    public boolean containsHeader(String s) {
        return false;
    }

    @Override
    public Header[] getHeaders(String s) {
        return new Header[0];
    }

    @Override
    public Header getFirstHeader(String s) {
        return null;
    }

    @Override
    public Header getLastHeader(String s) {
        return null;
    }

    @Override
    public Header[] getAllHeaders() {
        return new Header[0];
    }

    @Override
    public void addHeader(Header header) {

    }

    @Override
    public void addHeader(String s, String s1) {

    }

    @Override
    public void setHeader(Header header) {

    }

    @Override
    public void setHeader(String s, String s1) {

    }

    @Override
    public void setHeaders(Header[] headers) {

    }

    @Override
    public void removeHeader(Header header) {

    }

    @Override
    public void removeHeaders(String s) {

    }

    @Override
    public HeaderIterator headerIterator() {
        return null;
    }

    @Override
    public HeaderIterator headerIterator(String s) {
        return null;
    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    public void setParams(HttpParams httpParams) {

    }
}
