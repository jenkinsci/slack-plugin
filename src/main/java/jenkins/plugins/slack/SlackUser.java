package jenkins.plugins.slack;

import org.json.JSONObject;

public class SlackUser {
  private String id;
  private String name;
  private String real_name;
  private String mail;
  boolean deleted;

  public SlackUser(JSONObject json) {
    this.id = json.optString("id");
    this.name = json.optString("name");
    this.real_name = json.optString("real_name");
    this.mail = json.getJSONObject("profile").optString("email");
    this.deleted = json.optBoolean("deleted");
  }

  public String getName() {
    return name;
  }

  public String getMail() {
    return mail;
  }

  public boolean isDeleted() {
    return deleted;
  }
}
