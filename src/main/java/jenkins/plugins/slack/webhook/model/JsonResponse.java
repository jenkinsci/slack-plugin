package jenkins.plugins.slack.webhook.model;


import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

import net.sf.json.JSONObject;

import javax.servlet.ServletException;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;




public class JsonResponse implements HttpResponse {
    private int status;

    private String json;

    public JsonResponse(Object obj, int status) {
        this.json = null;
        try {
            this.json = new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            this.json = new JSONObject().put("text", ex.getMessage()).toString();
        } 
        this.status = status;
    }

    @Override
    public void generateResponse(StaplerRequest req,
        StaplerResponse rsp,
        Object o) throws IOException, ServletException {

        rsp.setStatus(status);
        rsp.setContentType("application/json;charset=UTF-8");
        rsp.getWriter().println(json);
    }

}
