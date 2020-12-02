package jenkins.plugins.slack;

import hudson.ProxyConfiguration;
import java.util.regex.Pattern;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.protocol.HttpContext;

public class NoProxyHostCheckerRoutePlanner implements HttpRoutePlanner {

    private DefaultProxyRoutePlanner defaultProxyRoutePlanner = null;
    private DefaultRoutePlanner defaultRoutePlanner = null;
    private String noProxyHost = null;

    public NoProxyHostCheckerRoutePlanner(String noProxyHost, HttpHost host){
        defaultProxyRoutePlanner = new DefaultProxyRoutePlanner(host);
        defaultRoutePlanner = new DefaultRoutePlanner(new DefaultSchemePortResolver());
        this.noProxyHost = noProxyHost;
    }

    public void setProxy(HttpHost host){
        defaultProxyRoutePlanner = new DefaultProxyRoutePlanner(host);
    }

    public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws org.apache.http.HttpException {
        final String targetHostUri = target.toURI();
        if(isNoProxyHost(targetHostUri))
            return defaultRoutePlanner.determineRoute(target,request,context);
        return defaultProxyRoutePlanner.determineRoute(target,request,context);
    }

    private boolean isNoProxyHost(String host) {
        if (host!=null && noProxyHost!=null) {
            for (Pattern p : ProxyConfiguration.getNoProxyHostPatterns(noProxyHost)) {
                if (p.matcher(host).matches()) {
                    return true;
                }
            }
        }
        return false;
    }
}
