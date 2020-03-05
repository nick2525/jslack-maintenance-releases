package test_with_remote_apis.methods;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.response.views.ViewsOpenResponse;
import com.github.seratch.jslack.api.methods.response.views.ViewsPublishResponse;
import com.github.seratch.jslack.api.methods.response.views.ViewsPushResponse;
import com.github.seratch.jslack.api.methods.response.views.ViewsUpdateResponse;
import com.github.seratch.jslack.api.model.view.View;
import config.Constants;
import config.SlackTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
public class views_Test {

    static SlackTestConfig testConfig = SlackTestConfig.getInstance();
    static Slack slack = Slack.getInstance(testConfig.getConfig());

    @AfterClass
    public static void tearDown() throws InterruptedException {
        SlackTestConfig.awaitCompletion(testConfig);
    }

    String botToken = System.getenv(Constants.SLACK_SDK_TEST_BOT_TOKEN);

    /*
     * A view in Slack can only be opened in response to a user action such as a slash command or
     * button click (which now include trigger_ids in callbacks). A view.open request has to include
     * that same trigger_id in order to succeed. The views.* request must also be made within 3
     * seconds of the user action.  Therefore, only an 'invalid trigger' ID response can be tested.
     */

    @Test
    public void open() throws IOException, SlackApiException {
        View view = View.builder().id("FAKE_ID").build();
        ViewsOpenResponse response = slack.methods().viewsOpen(r -> r
                .token(botToken)
                .triggerId("FAKE_TRIGGER_ID")
                .view(view));
        assertThat(response.isOk(), is(false));
        assertThat(response.getError(), is("invalid_arguments"));
    }

    @Test
    public void push() throws IOException, SlackApiException {
        View view = View.builder().id("FAKE_ID").build();
        ViewsPushResponse response = slack.methods().viewsPush(r -> r
                .token(botToken)
                .triggerId("FAKE_TRIGGER_ID")
                .view(view));
        assertThat(response.isOk(), is(false));
        assertThat(response.getError(), is("invalid_arguments"));
    }

    @Test
    public void update() throws IOException, SlackApiException {
        View view = View.builder().id("FAKE_ID").build();
        ViewsUpdateResponse response = slack.methods().viewsUpdate(r -> r
                .token(botToken)
                .externalId("FAKE_EXTERNAL_ID")
                .hash("FAKE_HASH")
                .viewId("FAKE_VIEW_ID")
                .view(view));
        assertThat(response.isOk(), is(false));
        assertThat(response.getError(), is("invalid_arguments"));
    }

    @Test
    public void publish() throws IOException, SlackApiException {
        View view = View.builder().id("FAKE_ID").build();
        ViewsPublishResponse response = slack.methods().viewsPublish(r -> r
                .token(botToken)
                .userId("FAKE_USER_ID")
                .view(view));
        assertThat(response.isOk(), is(false));
        // need to join the beta
        assertThat(response.getError(), is("invalid_arguments"));
    }

}
