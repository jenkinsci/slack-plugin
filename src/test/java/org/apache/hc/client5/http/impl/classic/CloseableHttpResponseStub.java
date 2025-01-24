package org.apache.hc.client5.http.impl.classic;

import java.util.Iterator;
import java.util.Locale;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ProtocolVersion;

public class CloseableHttpResponseStub implements ClassicHttpResponse {

    private HttpEntity entity;
    private int responseCode;

    public CloseableHttpResponseStub(int response) {
        this.responseCode = response;
        this.entity = null;
    }

    public CloseableHttpResponse toCloseableHttpResponse() {
        return CloseableHttpResponse.adapt(this);
    }

    @Override
    public void close() {

    }

    @Override
    public HttpEntity getEntity() {
        return entity;
    }

    @Override
    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    @Override
    public int getCode() {
        return responseCode;
    }

    @Override
    public void setCode(int code) {
        responseCode = code;
    }

    @Override
    public String getReasonPhrase() {
        return "";
    }

    @Override
    public void setReasonPhrase(String reason) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public void setVersion(ProtocolVersion version) {

    }

    @Override
    public ProtocolVersion getVersion() {
        return null;
    }

    @Override
    public void addHeader(Header header) {

    }

    @Override
    public void addHeader(String name, Object value) {

    }

    @Override
    public void setHeader(Header header) {

    }

    @Override
    public void setHeader(String name, Object value) {

    }

    @Override
    public void setHeaders(Header... headers) {

    }

    @Override
    public boolean removeHeader(Header header) {
        return false;
    }

    @Override
    public boolean removeHeaders(String name) {
        return false;
    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public int countHeaders(String name) {
        return 0;
    }

    @Override
    public Header getFirstHeader(String name) {
        return null;
    }

    @Override
    public Header getHeader(String name) {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return new Header[0];
    }

    @Override
    public Header[] getHeaders(String name) {
        return new Header[0];
    }

    @Override
    public Header getLastHeader(String name) {
        return null;
    }

    @Override
    public Iterator<Header> headerIterator() {
        return null;
    }

    @Override
    public Iterator<Header> headerIterator(String name) {
        return null;
    }
}
