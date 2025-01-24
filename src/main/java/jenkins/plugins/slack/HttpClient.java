package jenkins.plugins.slack;

import hudson.ProxyConfiguration;
import hudson.util.Secret;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Restricted(NoExternalUse.class)
public class HttpClient {

    public static HttpClientBuilder getCloseableHttpClientBuilder(ProxyConfiguration proxy) {
        int timeoutInSeconds = 60;

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(timeoutInSeconds))
                .setSocketTimeout(Timeout.ofSeconds(timeoutInSeconds)).build();
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(timeoutInSeconds)).build();

        final HttpClientBuilder clientBuilder = HttpClients
                .custom()
                .useSystemProperties()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig);
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);

        if (proxy != null) {
            final HttpHost proxyHost = new HttpHost(proxy.name, proxy.port);
            final HttpRoutePlanner routePlanner = new NoProxyHostCheckerRoutePlanner(proxy.getNoProxyHost(), proxyHost);
            clientBuilder.setRoutePlanner(routePlanner);

            String username = proxy.getUserName();
            Secret secretPassword = proxy.getSecretPassword();
            String password = Secret.toString(secretPassword);
            // Consider it to be passed if username specified. Sufficient?
            if (username != null && !username.trim().isEmpty()) {
                credentialsProvider.setCredentials(new AuthScope(proxyHost),
                        createCredentials(username, password));
            }
        }
        return clientBuilder;

    }

    public static CloseableHttpClient getCloseableHttpClient(ProxyConfiguration proxy) {
        return getCloseableHttpClientBuilder(proxy).build();
    }

    private static Credentials createCredentials(String userName, String password) {
        if (userName.indexOf('\\') >= 0){
            final String domain = userName.substring(0, userName.indexOf('\\'));
            final String user = userName.substring(userName.indexOf('\\') + 1);
            return new NTCredentials(user, password.toCharArray(), "", domain);
        } else {
            return new UsernamePasswordCredentials(userName, password.toCharArray());
        }
    }
}
