package mops.portfolios.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import mops.portfolios.PortfoliosApplication;
import mops.portfolios.domain.group.Group;
import mops.portfolios.domain.group.GroupRepository;
import mops.portfolios.domain.state.StateService;
import mops.portfolios.domain.user.User;
import mops.portfolios.domain.user.UserRepository;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class DatabaseUpdaterTest {

  private transient DatabaseUpdater databaseUpdater;

  /** The url to retrieve the data from */
  private transient String url = "/gruppen2/groupmembers";
  private transient static final Logger logger = (Logger) LoggerFactory.getLogger(PortfoliosApplication.class);


  private transient GroupRepository groupRepository = mock(GroupRepository.class);

  private transient UserRepository userRepository = mock(UserRepository.class);

  private transient StateService stateService = mock(StateService.class);

  @BeforeEach
  private void init() {

    databaseUpdater = new DatabaseUpdater(groupRepository, userRepository, stateService);

  }

  @Test
  void testClientError() {
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();


    listAppender.start();
    logger.addAppender(listAppender);
    IHttpClient httpClient = new FakeHttpClient();
    databaseUpdater.getGroupUpdatesFromUrl(httpClient, "400");
    listAppender.stop();

    List<ILoggingEvent> logsList = listAppender.list;
    int logSize = logsList.size();

    assertEquals("The service gruppen2 is not reachable: 400 BAD_REQUEST",
            logsList.get(logSize - 1).getMessage());
  }

  @Test
  void testEmptyJson() {
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    listAppender.start();
    logger.addAppender(listAppender);
    databaseUpdater.updateDatabaseEvents("{\"status\":4,\"groupList\":[]}");
    listAppender.stop();

    List<ILoggingEvent> logsList = listAppender.list;
    int logSize = logsList.size();

    assertEquals("Database not modified", logsList.get(logSize - 1).getMessage());
  }

  @Test
  void objectNotJson() {
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    listAppender.start();
    logger.addAppender(listAppender);
    databaseUpdater.updateDatabaseEvents("blabla");
    listAppender.stop();

    List<ILoggingEvent> logsList = listAppender.list;
    int logSize = logsList.size();

    assertEquals("An error occured while parsing the JSON data received by the service gruppen2",
            logsList.get(logSize - 1).getMessage());
  }

  @Test
  void testUpdateDatabaseEventsIllegalArgument() {
    databaseUpdater.url = new Url("http://bla/bla/");

    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    listAppender.start();
    logger.addAppender(listAppender);
    databaseUpdater.execute();
    listAppender.stop();

    List<ILoggingEvent> logsList = listAppender.list;
    int logSize = logsList.size();

    assertEquals("Some Exception occured while connecting to the host",
            logsList.get(logSize - 1).getMessage());
  }

  @Test
  void testSuccessfulRequest() {
    IHttpClient httpClient = new FakeHttpClient();
    databaseUpdater.getGroupUpdatesFromUrl(httpClient, this.url); // takes mocked JSON from the FakeHttpClient
  }

  @Test
  void GroupListNotModified() {
    // as talked with gruppen2, this is how the response will look if not modified
    String response = "{\"status\":4,\"groupList\":[]}";
    JSONObject jsonObject = new JSONObject(response);

    boolean result = databaseUpdater.isNotModified(jsonObject);
    assertTrue(result);
  }

  @SuppressWarnings("PMD")
  @Test
  void GroupListIsModified() {
    String response = "{\n" +
            "  \"status\": 4,\n" +
            "  \"groupList\": [\n" +
            "    {\n" +
            "      \"id\": \"0c69708c-6a48-4cfe-a5d8-c9e2b8220a80\",\n" +
            "      \"title\": null,\n" +
            "      \"description\": null,\n" +
            "      \"members\": [\n" +
            "        {\n" +
            "          \"user_id\": \"studentin\",\n" +
            "          \"givenname\": \"studentin\",\n" +
            "          \"familyname\": \"studentin\",\n" +
            "          \"email\": \"studentin@student.in\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"roles\": {\n" +
            "\"studentin\": \"ADMIN\"" +
            "},\n"+
            "      \"type\": \"LECTURE\",\n" +
            "      \"visibility\": \"PUBLIC\",\n" +
            "      \"parent\": null\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    JSONObject jsonObject = new JSONObject(response);

    boolean result = databaseUpdater.isNotModified(jsonObject);
    assertFalse(result);
  }

  @SuppressWarnings("PMD")
  @Test
  void extractJsonObject() {
    String response = "{\n" +
            "  \"status\": 4,\n" +
            "  \"groupList\": [\n" +
            "    {\n" +
            "      \"id\": \"0c69708c-6a48-4cfe-a5d8-c9e2b8220a80\",\n" +
            "      \"title\": \"Lorem\",\n" +
            "      \"description\": null,\n" +
            "      \"members\": [\n" +
            "        {\n" +
            "          \"user_id\": \"studentin\",\n" +
            "          \"givenname\": \"studentin\",\n" +
            "          \"familyname\": \"studentin\",\n" +
            "          \"email\": \"studentin@student.in\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"roles\": {\n" +
            "\"studentin\": \"ADMIN\"" +
            "},\n"+
            "      \"type\": \"LECTURE\",\n" +
            "      \"visibility\": \"PUBLIC\",\n" +
            "      \"parent\": null\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    databaseUpdater.updateDatabaseEvents(response);
  }

  @SuppressWarnings("PMD")
  @Test
  void deletedGroupTest() {
    List<User> userList = new ArrayList<>();
    User user = new User();
    user.setName("studentin");
    userList.add(user);

    when(groupRepository.save(any(Group.class))).thenReturn(new Group(2L, "Lorem", userList));

    groupRepository.save(new Group(2L, "Lorem", userList));

    String response = "{\n" +
            "  \"status\": 4,\n" +
            "  \"groupList\": [\n" +
            "    {\n" +
            "      \"id\": \"0c69708c-6a48-4cfe-a5d8-c9e2b8220a80\",\n" +
            "      \"title\": null,\n" +
            "      \"description\": null,\n" +
            "      \"members\": [\n" +
            "        {\n" +
            "          \"user_id\": \"studentin\",\n" +
            "          \"givenname\": \"studentin\",\n" +
            "          \"familyname\": \"studentin\",\n" +
            "          \"email\": \"studentin@student.in\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"roles\": {\n" +
            "\"studentin\": \"ADMIN\"" +
            "},\n"+
            "      \"type\": \"LECTURE\",\n" +
            "      \"visibility\": \"PUBLIC\",\n" +
            "      \"parent\": null\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    databaseUpdater.updateDatabaseEvents(response);

    List<Long> groupIds = new ArrayList<>();
    groupIds.add(2L);

    when(groupRepository.findAllById(groupIds)).thenReturn(new ArrayList<>());
    verify(groupRepository, times(2)).save(any(Group.class));

    Assert.assertEquals(new ArrayList<>(), groupRepository.findAllById(groupIds));

    verify(groupRepository, times(1)).findAllById(groupIds);

  }

  @SuppressWarnings("PMD")
  @Test
  void updateGroupTest() {

    List<User> userList = new ArrayList<>();
    User user = new User();
    user.setName("studentin");
    userList.add(user);

    when(userRepository.save(any(User.class))).thenReturn(user);

    userRepository.save(user);

    Group group = new Group(2L, "Lorem", userList);
    when(groupRepository.save(any(Group.class))).thenReturn(group);

    groupRepository.save(group);

    String response = "{\n" +
            "  \"status\": 4,\n" +
            "  \"groupList\": [\n" +
            "    {\n" +
            "      \"id\": \"0c69708c-6a48-4cfe-a5d8-c9e2b8220a80\",\n" +
            "      \"title\": null,\n" +
            "      \"description\": null,\n" +
            "      \"members\": [\n" +
            "        {\n" +
            "          \"user_id\": \"student\",\n" +
            "          \"givenname\": \"studentin\",\n" +
            "          \"familyname\": \"studentin\",\n" +
            "          \"email\": \"studentin@student.in\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"roles\": {\n" +
            "\"studentin\": \"ADMIN\"" +
            "},\n"+
            "      \"type\": \"LECTURE\",\n" +
            "      \"visibility\": \"PUBLIC\",\n" +
            "      \"parent\": null\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    databaseUpdater.updateDatabaseEvents(response);

    List<User> users = new ArrayList<>();
    User user1 = new User();
    user1.setName("student");
    users.add(user1);

    Group updatedGroup = new Group(2L, "Lorem", users);
    when(groupRepository.findById(2L)).thenReturn(Optional.of(updatedGroup));

    List<User> updatedGroupUsers = updatedGroup.getUsers();

    for (User userUpdatedGroup: updatedGroupUsers) {
      Assert.assertEquals("student", userUpdatedGroup.getName());

    }

  }

  @SuppressWarnings("PMD")
  @Test
  void saveNewGroupTest() {

    List<Long> groupIds = new ArrayList<>();
    groupIds.add(2L);

    List<User> userList = new ArrayList<>();
    User user = new User();
    user.setName("studentin");
    userList.add(user);

    List<Group> groups = new ArrayList<>();
    groups.add(new Group(2L, "Lorem", userList));

    String response = "{\n" +
            "  \"status\": 4,\n" +
            "  \"groupList\": [\n" +
            "    {\n" +
            "      \"id\": \"0c69708c-6a48-4cfe-a5d8-c9e2b8220a80\",\n" +
            "      \"title\": null,\n" +
            "      \"description\": null,\n" +
            "      \"members\": [\n" +
            "        {\n" +
            "          \"user_id\": \"studentin\",\n" +
            "          \"givenname\": \"studentin\",\n" +
            "          \"familyname\": \"studentin\",\n" +
            "          \"email\": \"studentin@student.in\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"roles\": {\n" +
            "\"studentin\": \"ADMIN\"" +
            "},\n"+
            "      \"type\": \"LECTURE\",\n" +
            "      \"visibility\": \"PUBLIC\",\n" +
            "      \"parent\": null\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    databaseUpdater.updateDatabaseEvents(response);

    when(groupRepository.findAllById(groupIds)).thenReturn(groups);
    List<Group> groupsFromRepository = (List<Group>) groupRepository.findAllById(groupIds);

    Assert.assertEquals(groups, groupsFromRepository);
  }

  @Test
  void testInvalidPort() {
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    listAppender.start();
    logger.addAppender(listAppender);
    IHttpClient httpClient = new HttpClient();
    databaseUpdater.getGroupUpdatesFromUrl(httpClient, "http://localhost:8083/gruppen2/");
    listAppender.stop();

    List<ILoggingEvent> logsList = listAppender.list;
    int logSize = logsList.size();

    assertEquals("Some Exception occured while connecting to the host",
            logsList.get(logSize - 1).getMessage());
  }

}
