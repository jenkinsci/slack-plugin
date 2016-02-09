package jenkins.plugins.slack;

import org.apache.commons.httpclient.HttpMethod;

import java.io.IOException;

public interface Client {

    ClientResponse request(HttpMethod httpMethod) throws IOException;

}
