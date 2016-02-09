package jenkins.plugins.slack;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;

public class ClientStub implements Client {

    private int numberOfCallsToExecuteMethod;
    private boolean failAlternateResponses = false;
    private ClientResponse clientResponse = new ClientResponse(HttpStatus.SC_OK, "");

    public ClientResponse request(HttpMethod httpMethod) {
        numberOfCallsToExecuteMethod++;
        if (failAlternateResponses && (numberOfCallsToExecuteMethod % 2 == 0)) {
            return new ClientResponse(HttpStatus.SC_NOT_FOUND, "");
        } else {
            return this.clientResponse;
        }
    }

    public int getNumberOfCallsToExecuteMethod() {
        return numberOfCallsToExecuteMethod;
    }

    public void setClientResponse(ClientResponse clientResponse) {
        this.clientResponse = clientResponse;
    }

    public void setFailAlternateResponses(boolean failAlternateResponses) {
        this.failAlternateResponses = failAlternateResponses;
    }
}
