package test_with_remote_apis.methods;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.SlackConfig;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.response.auth.AuthRevokeResponse;
import com.github.seratch.jslack.api.methods.response.auth.AuthTestResponse;
import config.Constants;
import config.SlackTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
public class auth_Test {

    static SlackTestConfig testConfig = SlackTestConfig.getInstance();
    static Slack slack = Slack.getInstance(testConfig.getConfig());

    @AfterClass
    public static void tearDown() throws InterruptedException {
        SlackTestConfig.awaitCompletion(testConfig);
    }

    @Test
    public void authRevoke() throws IOException, SlackApiException {
        AuthRevokeResponse response = slack.methods().authRevoke(req -> req.token("dummy").test(true));
        assertThat(response.isOk(), is(false));
        assertThat(response.getError(), is("invalid_auth"));
        assertThat(response.isRevoked(), is(false));
    }

    @Test
    public void authTest_user() throws IOException, SlackApiException {
        String userToken = System.getenv(Constants.SLACK_SDK_TEST_USER_TOKEN);
        AuthTestResponse response = slack.methods().authTest(req -> req.token(userToken));
        assertThat(response.getError(), is(nullValue()));
        assertThat(response.isOk(), is(true));
        assertThat(response.getUrl(), is(notNullValue()));
    }

    @Test
    public void authTest_user_2() throws IOException, SlackApiException {
        String userToken = System.getenv(Constants.SLACK_SDK_TEST_USER_TOKEN);
        AuthTestResponse response = slack.methods(userToken).authTest(req -> req);
        assertThat(response.getError(), is(nullValue()));
        assertThat(response.isOk(), is(true));
        assertThat(response.getUrl(), is(notNullValue()));
    }

    @Test
    public void authTest_bot() throws IOException, SlackApiException {
        String botToken = System.getenv(Constants.SLACK_SDK_TEST_BOT_TOKEN);
        AuthTestResponse response = slack.methods(botToken).authTest(req -> req);
        assertThat(response.getError(), is(nullValue()));
        assertThat(response.isOk(), is(true));
        assertThat(response.getUrl(), is(notNullValue()));
    }

    @Test
    public void authTest_grid() throws IOException, SlackApiException {
        String orgAdminToken = System.getenv(Constants.SLACK_SDK_TEST_GRID_WORKSPACE_ADMIN_USER_TOKEN);
        AuthTestResponse response = slack.methods().authTest(req -> req.token(orgAdminToken));
        assertThat(response.getError(), is(nullValue()));
        assertThat(response.isOk(), is(true));
        assertThat(response.getUrl(), is(notNullValue()));
    }

    @Test(expected = IllegalStateException.class)
    public void authTest_missingToken() throws IOException, SlackApiException {
        SlackConfig config = new SlackConfig();
        config.setTokenExistenceVerificationEnabled(true);
        Slack slack = Slack.getInstance(config);
        slack.methods().authTest(req -> req);
    }

}
