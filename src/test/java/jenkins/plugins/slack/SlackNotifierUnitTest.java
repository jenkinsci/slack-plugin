package jenkins.plugins.slack;

import hudson.util.FormValidation;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SlackNotifierUnitTest {
    private SlackNotifier slackNotifier = new SlackNotifier(CommitInfoChoice.AUTHORS);
    private SlackNotifierStub.DescriptorImplStub descriptor;

    @Before
    public void setUp() {
        descriptor = new SlackNotifierStub.DescriptorImplStub();
    }

    @Test
    public void isAnyCustomMessagePopulatedReturnsFalseWhenNonePopulated() {
        assertFalse(slackNotifier.isAnyCustomMessagePopulated());
    }

    @Test
    public void isAnyCustomMessagePopulatedReturnsTrueWhenOnePopulated() {
        slackNotifier.setCustomMessage("hi");

        assertTrue(slackNotifier.isAnyCustomMessagePopulated());
    }

    @Test
    public void isAnyCustomMessagePopulatedReturnsTrueWhenMultiplePopulated() {
        slackNotifier.setCustomMessage("hi");
        slackNotifier.setCustomMessageFailure("hii");

        assertTrue(slackNotifier.isAnyCustomMessagePopulated());
    }

    @Test
    public void doCheckBaseUrl_emptyIsValid() {
        FormValidation formValidation = descriptor.doCheckBaseUrl("", null);

        assertThat(formValidation.kind, is(FormValidation.Kind.OK));
    }

    @Test
    public void doCheckBaseUrl_setIsValid() {
        FormValidation formValidation = descriptor.doCheckBaseUrl("", null);

        assertThat(formValidation.kind, is(FormValidation.Kind.OK));
    }

    @Test
    public void doCheckBaseUrl_baseUrlAndTeamDomainSet_is_invalid() {
        FormValidation formValidation = descriptor.doCheckBaseUrl("a", "a");

        assertThat(formValidation.kind, is(FormValidation.Kind.ERROR));
    }

    @Test
    public void doCheckBaseUrl_teamDomain_only_set_is_valid() {
        FormValidation formValidation = descriptor.doCheckBaseUrl(null, "a");

        assertThat(formValidation.kind, is(FormValidation.Kind.OK));
    }

    @Test
    public void doCheckBaseUrl_including_jenkins_ci_hook_is_invalid() {
        FormValidation formValidation = descriptor.doCheckBaseUrl("https://jenkins-slack-plugin-testing.slack.com/services/hooks/jenkins-ci", null);

        assertThat(formValidation.kind, is(FormValidation.Kind.ERROR));
    }
}
