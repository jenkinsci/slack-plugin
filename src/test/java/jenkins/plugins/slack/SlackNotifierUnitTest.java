package jenkins.plugins.slack;

import hudson.util.FormValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlackNotifierUnitTest {
    private SlackNotifier slackNotifier = new SlackNotifier(CommitInfoChoice.AUTHORS);
    private SlackNotifierStub.DescriptorImplStub descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new SlackNotifierStub.DescriptorImplStub();
    }

    @Test
    void isAnyCustomMessagePopulatedReturnsFalseWhenNonePopulated() {
        assertFalse(slackNotifier.isAnyCustomMessagePopulated());
    }

    @Test
    void isAnyCustomMessagePopulatedReturnsTrueWhenOnePopulated() {
        slackNotifier.setCustomMessage("hi");

        assertTrue(slackNotifier.isAnyCustomMessagePopulated());
    }

    @Test
    void isAnyCustomMessagePopulatedReturnsTrueWhenMultiplePopulated() {
        slackNotifier.setCustomMessage("hi");
        slackNotifier.setCustomMessageFailure("hii");

        assertTrue(slackNotifier.isAnyCustomMessagePopulated());
    }

    @Test
    void doCheckBaseUrl_emptyIsValid() {
        FormValidation formValidation = descriptor.doCheckBaseUrl("", null);

        assertThat(formValidation.kind, is(FormValidation.Kind.OK));
    }

    @Test
    void doCheckBaseUrl_setIsValid() {
        FormValidation formValidation = descriptor.doCheckBaseUrl("", null);

        assertThat(formValidation.kind, is(FormValidation.Kind.OK));
    }

    @Test
    void doCheckBaseUrl_baseUrlAndTeamDomainSet_is_invalid() {
        FormValidation formValidation = descriptor.doCheckBaseUrl("a", "a");

        assertThat(formValidation.kind, is(FormValidation.Kind.ERROR));
    }

    @Test
    void doCheckBaseUrl_teamDomain_only_set_is_valid() {
        FormValidation formValidation = descriptor.doCheckBaseUrl(null, "a");

        assertThat(formValidation.kind, is(FormValidation.Kind.OK));
    }

    @Test
    void doCheckBaseUrl_including_jenkins_ci_hook_is_invalid() {
        FormValidation formValidation = descriptor.doCheckBaseUrl("https://jenkins-slack-plugin-testing.slack.com/services/hooks/jenkins-ci", null);

        assertThat(formValidation.kind, is(FormValidation.Kind.ERROR));
    }
}
