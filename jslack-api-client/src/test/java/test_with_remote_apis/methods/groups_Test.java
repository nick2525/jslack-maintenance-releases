package test_with_remote_apis.methods;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.groups.*;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.groups.*;
import com.github.seratch.jslack.api.model.Group;
import com.github.seratch.jslack.api.model.User;
import config.Constants;
import config.SlackTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
public class groups_Test {

    static SlackTestConfig testConfig = SlackTestConfig.getInstance();
    static Slack slack = Slack.getInstance(testConfig.getConfig());

    @AfterClass
    public static void tearDown() throws InterruptedException {
        SlackTestConfig.awaitCompletion(testConfig);
    }

    String userToken = System.getenv(Constants.SLACK_SDK_TEST_USER_TOKEN);

    @Test
    public void groups() throws IOException, SlackApiException {

        String name = "secret-" + System.currentTimeMillis();
        GroupsCreateResponse creationResponse = slack.methods().groupsCreate(r -> r.token(userToken).name(name));
        Group group = creationResponse.getGroup();
        {
            assertThat(creationResponse.getError(), is(nullValue()));
            assertThat(creationResponse.isOk(), is(true));
            assertThat(creationResponse.getGroup(), is(notNullValue()));
        }

        {
            GroupsListResponse response = slack.methods().groupsList(
                    GroupsListRequest.builder().token(userToken).build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }
        {
            GroupsListResponse response = slack.methods().groupsList(
                    GroupsListRequest.builder().token(userToken).excludeArchived(true).build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }

        {
            GroupsHistoryResponse response = slack.methods().groupsHistory(
                    GroupsHistoryRequest.builder().token(userToken).channel(group.getId()).count(10).build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }

        {
            GroupsInfoResponse response = slack.methods().groupsInfo(
                    GroupsInfoRequest.builder().token(userToken).channel(group.getId()).build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }

        GroupsCreateChildResponse childCreationResponse = slack.methods().groupsCreateChild(
                GroupsCreateChildRequest.builder().token(userToken).channel(group.getId()).build());
        group = childCreationResponse.getGroup();
        {
            assertThat(childCreationResponse.getError(), is(nullValue()));
            assertThat(childCreationResponse.isOk(), is(true));
        }

        {
            String groupId = childCreationResponse.getGroup().getId();
            ChatPostMessageResponse postResponse = slack.methods().chatPostMessage(r -> r
                    .token(userToken)
                    .text("How are you?")
                    .channel(groupId));
            assertThat(postResponse.getError(), is(nullValue()));

            ChatPostMessageResponse replyResponse = slack.methods().chatPostMessage(r -> r
                    .token(userToken)
                    .threadTs(postResponse.getTs())
                    .text("Great! How are you?")
                    .channel(groupId));
            assertThat(replyResponse.getError(), is(nullValue()));

            String ts = postResponse.getTs();
            GroupsMarkResponse response = slack.methods().groupsMark(
                    GroupsMarkRequest.builder().token(userToken).channel(group.getId()).ts(ts).build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));

            GroupsRepliesResponse repliesResponse = slack.methods().groupsReplies(r -> r
                    .token(userToken)
                    .channel(groupId)
                    .threadTs(postResponse.getTs()));
            assertThat(repliesResponse.getError(), is(nullValue()));
            assertThat(repliesResponse.isOk(), is(true));
        }

        {
            GroupsRenameResponse response = slack.methods().groupsRename(
                    GroupsRenameRequest.builder().token(userToken).channel(group.getId()).name(name + "-").build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }

        {
            GroupsSetPurposeResponse response = slack.methods().groupsSetPurpose(
                    GroupsSetPurposeRequest.builder().token(userToken).channel(group.getId()).purpose("purpose").build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }

        {
            GroupsSetTopicResponse response = slack.methods().groupsSetTopic(
                    GroupsSetTopicRequest.builder().token(userToken).channel(group.getId()).topic("topic").build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }
        String userIdToInvite = null;
        List<User> users = slack.methods().usersList(r -> r.token(userToken).limit(100)).getMembers();
        for (User user : users) {
            boolean alreadyInTheGroup = false;
            for (String groupMember : group.getMembers()) {
                if (groupMember.equals(user.getId())) {
                    alreadyInTheGroup = true;
                    break;
                }
            }
            if (!alreadyInTheGroup && user.isBot()) {
                userIdToInvite = user.getId();
                break;
            }
        }
        {
            GroupsInviteResponse response = slack.methods().groupsInvite(GroupsInviteRequest.builder()
                    .token(userToken)
                    .channel(group.getId())
                    .user(userIdToInvite)
                    .build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }
        {
            GroupsKickResponse response = slack.methods().groupsKick(GroupsKickRequest.builder()
                    .token(userToken)
                    .channel(group.getId())
                    .user(userIdToInvite).build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }
        {
            GroupsOpenResponse response = slack.methods().groupsOpen(GroupsOpenRequest.builder()
                    .token(userToken)
                    .channel(group.getId())
                    .build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }
        {
            GroupsInviteResponse response = slack.methods().groupsInvite(GroupsInviteRequest.builder()
                    .token(userToken)
                    .channel(group.getId())
                    .user(userIdToInvite)
                    .build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }
        {
            GroupsLeaveResponse response = slack.methods().groupsLeave(GroupsLeaveRequest.builder()
                    .token(userToken)
                    .channel(group.getId())
                    .build());
            // TODO: failing
            assertThat(response.getError(), is("last_member"));
        }

        {
            GroupsArchiveResponse response = slack.methods().groupsArchive(
                    GroupsArchiveRequest.builder().token(userToken).channel(group.getId()).build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }

        {
            GroupsUnarchiveResponse response = slack.methods().groupsUnarchive(
                    GroupsUnarchiveRequest.builder().token(userToken).channel(group.getId()).build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }

        {
            // NOTE: https://github.com/slackapi/slack-api-specs/issues/12
            GroupsCloseResponse response = slack.methods().groupsClose(
                    GroupsCloseRequest.builder().token(userToken).channel(group.getId()).build());
            // No error is returned but this API seems to be useless.
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }

        {
            GroupsArchiveResponse response = slack.methods().groupsArchive(
                    GroupsArchiveRequest.builder().token(userToken).channel(group.getId()).build());
            assertThat(response.getError(), is(nullValue()));
            assertThat(response.isOk(), is(true));
        }
    }

}
