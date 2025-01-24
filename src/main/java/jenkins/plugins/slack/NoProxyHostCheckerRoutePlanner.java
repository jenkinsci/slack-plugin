package jenkins.plugins.slack;

import hudson.ProxyConfiguration;
import java.util.regex.Pattern;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;


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

    @Override
    public HttpRoute determineRoute(HttpHost target, HttpContext context) throws HttpException {
        final String targetHostUri = target.toURI();
        if(isNoProxyHost(targetHostUri))
            return defaultRoutePlanner.determineRoute(target, context);
        return defaultProxyRoutePlanner.determineRoute(target, context);
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
