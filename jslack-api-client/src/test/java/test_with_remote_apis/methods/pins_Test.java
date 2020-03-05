package test_with_remote_apis.methods;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.response.files.FilesUploadResponse;
import com.github.seratch.jslack.api.methods.response.pins.PinsAddResponse;
import com.github.seratch.jslack.api.methods.response.pins.PinsListResponse;
import com.github.seratch.jslack.api.methods.response.pins.PinsRemoveResponse;
import com.github.seratch.jslack.api.model.Channel;
import config.Constants;
import config.SlackTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
public class pins_Test {

    static SlackTestConfig testConfig = SlackTestConfig.getInstance();
    static Slack slack = Slack.getInstance(testConfig.getConfig());

    @AfterClass
    public static void tearDown() throws InterruptedException {
        SlackTestConfig.awaitCompletion(testConfig);
    }

    String botToken = System.getenv(Constants.SLACK_SDK_TEST_BOT_TOKEN);

    @Test
    public void list() throws IOException, SlackApiException {
        List<Channel> channels_ = slack.methods().channelsList(r -> r.token(botToken)).getChannels();
        List<String> channels = new ArrayList<>();
        for (Channel c : channels_) {
            if (c.getName().equals("random")) {
                channels.add(c.getId());
                break;
            }
        }

        PinsListResponse response = slack.methods().pinsList(
                r -> r.token(botToken).channel(channels.get(0)));
        assertThat(response.getError(), is(nullValue()));
        assertThat(response.isOk(), is(true));
        assertThat(response.getItems(), is(notNullValue()));
    }

    @Test
    public void add() throws IOException, SlackApiException {
        List<Channel> channels_ = slack.methods().channelsList(r -> r.token(botToken)).getChannels();
        List<String> channels = new ArrayList<>();
        for (Channel c : channels_) {
            if (c.getName().equals("random")) {
                channels.add(c.getId());
                break;
            }
        }

        File file = new File("src/test/resources/sample.txt");
        com.github.seratch.jslack.api.model.File fileObj;
        {
            FilesUploadResponse response = slack.methods().filesUpload(r -> r
                    .token(botToken)
                    .channels(channels)
                    .file(file)
                    .filename("sample.txt")
                    .initialComment("initial comment")
                    .title("file title"));
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
            fileObj = response.getFile();
        }

        {
            // https://api.slack.com/methods/pins.add
            PinsAddResponse response = slack.methods().pinsAdd(r -> r
                    .token(botToken)
                    .channel(channels.get(0))
                    .file(fileObj.getId()));
//            // We are phasing out support for pinning files and file comments only.
//            // This method will no longer accept the file and file_comment parameters beginning August 22, 2019.
//            assertThat(response.getError(), is("not_pinnable"));
            // Since Oct 2019
            assertThat(response.getError(), is(nullValue()));
        }
        {
            // https://api.slack.com/methods/pins.add
            PinsRemoveResponse response = slack.methods().pinsRemove(r -> r
                    .token(botToken)
                    .channel(channels.get(0))
                    .file(fileObj.getId()));
            // We are phasing out support for pinning files and file comments only.
            // This method will no longer accept the file and file_comment parameters beginning August 22, 2019.
//            assertThat(response.getError(), is("no_pin"));
            // Since Oct 2019
            assertThat(response.getError(), is(nullValue()));
        }

        {
            // as of August 2018, File object no longer contains initialComment.
            if (fileObj.getInitialComment() != null) {
                PinsAddResponse response = slack.methods().pinsAdd(r -> r
                        .token(botToken)
                        .channel(channels.get(0))
                        .fileComment(fileObj.getInitialComment().getId()));
                assertThat(response.getError(), is(nullValue()));
                assertThat(response.isOk(), is(true));
            }
        }

    }

}
