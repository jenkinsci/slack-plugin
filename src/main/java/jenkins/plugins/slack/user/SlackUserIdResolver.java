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

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractBuild;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Run;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jenkins.model.Jenkins;
import jenkins.scm.RunWithSCM;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;

public abstract class SlackUserIdResolver extends AbstractDescribableImpl<SlackUserIdResolver> implements ExtensionPoint {

    private static final Logger LOGGER = Logger.getLogger(SlackUserIdResolver.class.getName());

    private String authToken;
    private CloseableHttpClient httpClient;

    public final String findOrResolveUserId(User user) {
        String userId = null;
        SlackUserProperty userProperty = user.getProperty(SlackUserProperty.class);
        if (userProperty != null) {
            userId = userProperty.getUserId();
        } else {
            userProperty = new SlackUserProperty();
        }

        if (StringUtils.isEmpty(userId)) {
            userId = resolveUserId(user);
            if (userId != null) {
                userProperty.setUserId(userId);
                try {
                    user.addProperty(userProperty);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Failed to add SlackUserProperty to user: " + user.toString(), ex);
                }
            }
        }

        final boolean enableNotifications = !userProperty.getDisableNotifications();
        return enableNotifications ? userId : null;
    }

    protected abstract String resolveUserId(User user);

    @SuppressWarnings("unchecked")
    public List<String> resolveUserIdsForRun(Run run) {
        if (run instanceof RunWithSCM) {
            RunWithSCM r = (RunWithSCM) run;
            return resolveUserIdsForChangeLogSets(r.getChangeSets());
        } else if (run instanceof AbstractBuild) {
            AbstractBuild build = (AbstractBuild) run;
            return resolveUserIdsForChangeLogSets(build.getChangeSets());
        } else {
            return Collections.emptyList();
        }
    }

    public List<String> resolveUserIdsForChangeLogSet(ChangeLogSet changeLogSet) {
        return Arrays.stream(changeLogSet.getItems())
                .map(item -> findOrResolveUserId(((Entry) item).getAuthor()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<String> resolveUserIdsForChangeLogSets(List<ChangeLogSet> changeLogSets) {
        return changeLogSets.stream()
                .map(this::resolveUserIdsForChangeLogSet)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public SlackUserIdResolverDescriptor getDescriptor() {
        return (SlackUserIdResolverDescriptor) super.getDescriptor();
    }

    /**
     * All registered {@link SlackUserIdResolver}s.
     * @return all SlackUserIdResolver as an ExtensionList
     */
    public static ExtensionList<SlackUserIdResolver> all() {
        return Jenkins.get().getExtensionList(SlackUserIdResolver.class);
    }
}
