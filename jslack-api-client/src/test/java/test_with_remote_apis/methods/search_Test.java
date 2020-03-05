package test_with_remote_apis.methods;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.response.search.SearchAllResponse;
import com.github.seratch.jslack.api.methods.response.search.SearchFilesResponse;
import com.github.seratch.jslack.api.methods.response.search.SearchMessagesResponse;
import com.github.seratch.jslack.api.model.MatchedItem;
import config.Constants;
import config.SlackTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
public class search_Test {

    static SlackTestConfig testConfig = SlackTestConfig.getInstance();
    static Slack slack = Slack.getInstance(testConfig.getConfig());

    @AfterClass
    public static void tearDown() throws InterruptedException {
        SlackTestConfig.awaitCompletion(testConfig);
    }

    String userToken = System.getenv(Constants.SLACK_SDK_TEST_USER_TOKEN);

    @Test
    public void all() throws IOException, SlackApiException {
        SearchAllResponse response = slack.methods().searchAll(r -> r.token(userToken).query("test"));

        assertThat(response.getError(), is(nullValue()));
        assertThat(response.isOk(), is(true));
    }

    @Test
    public void messages() throws IOException, SlackApiException {
        SearchMessagesResponse response = slack.methods().searchMessages(r -> r.token(userToken).query("test"));

        assertThat(response.getError(), is(nullValue()));
        assertThat(response.isOk(), is(true));

        MatchedItem match = response.getMessages().getMatches().get(0);
        assertThat(match.getUsername(), is(notNullValue()));
        // As of Nov 2019, user is not available
        // assertThat(match.getUser(), is(notNullValue()));
    }

    @Test
    public void files() throws IOException, SlackApiException {
        SearchFilesResponse response = slack.methods().searchFiles(r -> r.token(userToken).query("test"));

        assertThat(response.getError(), is(nullValue()));
        assertThat(response.isOk(), is(true));

        MatchedItem match = response.getFiles().getMatches().get(0);
        assertThat(match.getUser(), is(notNullValue()));
        assertThat(match.getUsername(), is(notNullValue()));
    }
}
