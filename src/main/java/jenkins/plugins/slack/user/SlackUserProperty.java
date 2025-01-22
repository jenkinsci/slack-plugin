/*
 * The MIT License
 *
 * Copyright (C) 2019 Electronic Arts Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.plugins.slack.user;

import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.export.Exported;

public class SlackUserProperty extends UserProperty {

    private String userId;
    private boolean disableNotifications;

    @DataBoundConstructor
    public SlackUserProperty() {
    }

    @Exported
    public String getUserId() {
        return userId;
    }

    @DataBoundSetter
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Exported
    public boolean getDisableNotifications() {
        return disableNotifications;
    }

    @DataBoundSetter
    public void setDisableNotifications(boolean disableNotifications) {
        this.disableNotifications = disableNotifications;
    }

    @Extension
    public static class DescriptorImpl extends UserPropertyDescriptor {

        @Override
        public UserProperty newInstance(User user) {
            return new SlackUserProperty();
        }

        @Override
        public String getDisplayName() {
            return "Slack User Settings";
        }

        @Override
        public UserProperty newInstance(StaplerRequest2 req, JSONObject formData) {
            return req.bindJSON(SlackUserProperty.class, formData);
        }
    }
}
