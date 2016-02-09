package jenkins.plugins.slack;

public class ClientResponse {

    public String body;
    public int statusCode;

    public ClientResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
