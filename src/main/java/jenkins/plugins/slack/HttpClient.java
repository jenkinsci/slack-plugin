package jenkins.plugins.slack;

import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Restricted(NoExternalUse.class)
public class HttpClient {

    public static CloseableHttpClient getCloseableHttpClient() {
        int timeoutInSeconds = 60;

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeoutInSeconds * 1000)
                .setConnectionRequestTimeout(timeoutInSeconds * 1000)
                .setSocketTimeout(timeoutInSeconds * 1000).build();

        final HttpClientBuilder clientBuilder = HttpClients.custom().setDefaultRequestConfig(config);
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);

        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins != null) {
            ProxyConfiguration proxy = jenkins.proxy;
            if (proxy != null) {
                final HttpHost proxyHost = new HttpHost(proxy.name, proxy.port);
                final HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
                clientBuilder.setRoutePlanner(routePlanner);

                String username = proxy.getUserName();
                String password = proxy.getPassword();
                // Consider it to be passed if username specified. Sufficient?
                if (username != null && !"".equals(username.trim())) {
                    credentialsProvider.setCredentials(new AuthScope(proxyHost),
                            new UsernamePasswordCredentials(username, password));
                }
            }
        }
        return clientBuilder.build();
    }

}
