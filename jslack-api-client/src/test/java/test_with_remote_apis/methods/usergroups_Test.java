package test_with_remote_apis.methods;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.request.usergroups.users.UsergroupUsersListRequest;
import com.github.seratch.jslack.api.methods.response.usergroups.*;
import com.github.seratch.jslack.api.methods.response.usergroups.users.UsergroupUsersListResponse;
import com.github.seratch.jslack.api.methods.response.usergroups.users.UsergroupUsersUpdateResponse;
import com.github.seratch.jslack.api.methods.response.users.UsersListResponse;
import com.github.seratch.jslack.api.model.User;
import com.github.seratch.jslack.api.model.Usergroup;
import config.Constants;
import config.SlackTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
public class usergroups_Test {

    static SlackTestConfig testConfig = SlackTestConfig.getInstance();
    static Slack slack = Slack.getInstance(testConfig.getConfig());

    @AfterClass
    public static void tearDown() throws InterruptedException {
        SlackTestConfig.awaitCompletion(testConfig);
    }

    String botToken = System.getenv(Constants.SLACK_SDK_TEST_BOT_TOKEN);
    String userToken = System.getenv(Constants.SLACK_SDK_TEST_USER_TOKEN);

    @Test
    public void create() throws Exception {
        String usergroupName = "usergroup-" + System.currentTimeMillis();
        UsergroupsCreateResponse response = slack.methods().usergroupsCreate(r -> r
                .token(userToken)
                .name(usergroupName));
        if (response.isOk()) {
            assertThat(response.getUsergroup(), is(notNullValue()));
            assertThat(response.getUsergroup().getName(), is(usergroupName));
        } else {
            assertThat(response.getError(), is(anyOf(
                    // For a good old token, "paid_teams_only" can be returned as the error
                    equalTo("paid_teams_only"),
                    // As of 2018, this code is generally returned for newly created token
                    equalTo("missing_scope")
            )));
        }
    }

    @Test
    public void list() throws Exception {
        UsergroupsListResponse response = slack.methods().usergroupsList(r -> r.token(botToken));
        assertThat(response.getError(), is(nullValue()));
        assertThat(response.isOk(), is(true));
    }

    @Test
    public void usergroups() throws Exception {
        UsergroupsListResponse usergroups = slack.methods().usergroupsList(r -> r.token(userToken));
        if (usergroups.isOk() && usergroups.getUsergroups().size() > 0) {
            UsergroupUsersListResponse response = slack.methods().usergroupUsersList(
                    UsergroupUsersListRequest.builder()
                            .token(userToken)
                            .includeDisabled(false)
                            .usergroup(usergroups.getUsergroups().get(0).getId())
                            .build());
            assertThat(response.getError(), is(nullValue()));
        }

        UsergroupsCreateResponse creation = slack.methods().usergroupsCreate(r -> r
                .token(userToken)
                .name("usergroup-" + System.currentTimeMillis())
                .description("Something wrong"));
        assertThat(creation.getError(), is(nullValue()));
        final Usergroup usergroup = creation.getUsergroup();
        {
            UsergroupsDisableResponse response = slack.methods().usergroupsDisable(r -> r
                    .token(userToken)
                    .usergroup(usergroup.getId()));
            assertThat(response.getError(), is(nullValue()));
        }
        {
            UsergroupsEnableResponse response = slack.methods().usergroupsEnable(r -> r
                    .token(userToken)
                    .usergroup(usergroup.getId()));
            assertThat(response.getError(), is(nullValue()));
        }
        {
            UsergroupsUpdateResponse response = slack.methods().usergroupsUpdate(r -> r
                    .token(userToken)
                    .usergroup(usergroup.getId())
                    .description("updated"));
            assertThat(response.getError(), is(nullValue()));
        }
        {
            UsersListResponse usersListResponse = slack.methods().usersList(r -> r
                    .token(userToken)
                    .limit(3));
            List<String> userIds = new ArrayList<>();
            for (User member : usersListResponse.getMembers()) {
                userIds.add(member.getId());
            }
            UsergroupUsersUpdateResponse response = slack.methods().usergroupUsersUpdate(r -> r
                    .token(userToken)
                    .usergroup(usergroup.getId())
                    .users(userIds));
            assertThat(response.getError(), is(nullValue()));
        }
    }

    @Test
    public void users_failure() throws Exception {
        UsergroupUsersListResponse response = slack.methods().usergroupUsersList(
                UsergroupUsersListRequest.builder()
                        .token(botToken)
                        .includeDisabled(false)
                        .usergroup("dummy")
                        .build());
        assertThat(response.isOk(), is(false));
//            assertThat(response.getError(), is("missing_required_argument"));
        // As of 2018/05, the error message has been changed
        assertThat(response.getError(), is("no_such_subteam"));
    }

}
