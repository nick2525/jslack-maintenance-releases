package test_with_remote_apis.methods;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.reactions.ReactionsAddResponse;
import com.github.seratch.jslack.api.methods.response.reactions.ReactionsGetResponse;
import com.github.seratch.jslack.api.methods.response.reactions.ReactionsListResponse;
import com.github.seratch.jslack.api.methods.response.reactions.ReactionsRemoveResponse;
import config.Constants;
import config.SlackTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
public class reactions_Test {

    static SlackTestConfig testConfig = SlackTestConfig.getInstance();
    static Slack slack = Slack.getInstance(testConfig.getConfig());

    @AfterClass
    public static void tearDown() throws InterruptedException {
        SlackTestConfig.awaitCompletion(testConfig);
    }

    String botToken = System.getenv(Constants.SLACK_SDK_TEST_BOT_TOKEN);

    @Test
    public void test() throws IOException, SlackApiException {
        String channel = slack.methods().channelsList(r -> r.token(botToken).excludeArchived(true))
                .getChannels().get(0).getId();

        ChatPostMessageResponse postMessage = slack.methods().chatPostMessage(ChatPostMessageRequest.builder()
                .token(botToken)
                .channel(channel)
                .text("hello")
                .build());
        assertThat(postMessage.getError(), is(nullValue()));
        assertThat(postMessage.isOk(), is(true));

        String timestamp = postMessage.getTs();
        ReactionsAddResponse addResponse = slack.methods().reactionsAdd(r -> r
                .token(botToken)
                .name("smile")
                .channel(channel)
                .timestamp(timestamp));
        assertThat(addResponse.getError(), is(nullValue()));
        assertThat(addResponse.isOk(), is(true));

        ReactionsGetResponse getResponse = slack.methods().reactionsGet(r -> r
                .token(botToken)
                .channel(channel)
                .timestamp(timestamp));
        assertThat(getResponse.getError(), is(nullValue()));
        assertThat(getResponse.isOk(), is(true));

        ReactionsRemoveResponse removeResponse = slack.methods().reactionsRemove(r -> r
                .token(botToken)
                .name("smile")
                .channel(channel)
                .timestamp(timestamp));
        assertThat(removeResponse.getError(), is(nullValue()));
        assertThat(removeResponse.isOk(), is(true));

    }

    @Test
    public void list() throws IOException, SlackApiException {
        String user = slack.methods().usersList(r -> r.token(botToken))
                .getMembers().get(0).getId();

        ReactionsListResponse response = slack.methods().reactionsList(r -> r
                .token(botToken)
                .user(user));
        assertThat(response.getError(), is(nullValue()));
        assertThat(response.isOk(), is(true));
        assertThat(response.getItems(), is(notNullValue()));
    }

}
