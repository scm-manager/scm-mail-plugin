/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.mail.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.mail.api.Category;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.ScmTransportStrategy;
import sonia.scm.mail.api.SummaryFrequency;
import sonia.scm.mail.api.Topic;
import sonia.scm.mail.api.UserMailConfiguration;
import sonia.scm.user.User;
import sonia.scm.web.RestDispatcher;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import static java.util.Collections.singleton;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.resteasy.mock.MockHttpRequest.create;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.SCMContext.USER_ANONYMOUS;

@ExtendWith(MockitoExtension.class)
class MailConfigurationResourceTest {

  @Mock
  MailService mailService;
  @Mock
  MailContext context;

  RestDispatcher dispatcher = new RestDispatcher();

  MockHttpResponse response = new MockHttpResponse();

  private final ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void setupResource() {
    MailConfigurationMapperImpl mapper = new MailConfigurationMapperImpl();
    MailConfigurationResource resource = new MailConfigurationResource(mailService, context, mapper);
    dispatcher.addSingletonResource(resource);
  }

  @Nested
  class withAuthorizedUser {

    @BeforeEach
    void setupUser() {
      Subject subject = mock(Subject.class);
      PrincipalCollection principalCollection = mock(PrincipalCollection.class);
      lenient().when(principalCollection.oneByType(User.class)).thenReturn(new User("dent"));
      lenient().when(subject.getPrincipals()).thenReturn(principalCollection);
      ThreadContext.bind(subject);
    }

    @AfterEach
    void unbindSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldGetAllTopics() throws URISyntaxException, UnsupportedEncodingException {
      when(context.availableTopics()).thenReturn(singleton(new Topic(new Category("hitchhiker"), "towel")));

      dispatcher.invoke(create("GET", "/v2/plugins/mail/topics"), response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getContentAsString())
        .contains("\"_links\":{\"self\":{\"href\":\"/v2/plugins/mail/topics\"}}")
        .contains("\"topics\":[{\"category\":{\"name\":\"hitchhiker\"},\"name\":\"towel\"}]");
    }

    @Test
    void shouldReturnConfigurationForUser() throws URISyntaxException, UnsupportedEncodingException {
      UserMailConfiguration userMailConfiguration = new UserMailConfiguration();
      userMailConfiguration.setLanguage("vogon");
      userMailConfiguration.setSummarizeMails(true);
      userMailConfiguration.setSummaryFrequency(SummaryFrequency.HOURS_2);
      userMailConfiguration.setSummarizeByEntity(true);
      userMailConfiguration.setExcludedTopics(singleton(new Topic(new Category("hitchhiker"), "towel")));
      when(context.getUserConfiguration("dent")).thenReturn(of(userMailConfiguration));

      dispatcher.invoke(create("GET", "/v2/plugins/mail/user-config"), response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getContentAsString())
        .contains("\"self\":{\"href\":\"/v2/plugins/mail/user-config\"}")
        .contains("\"update\":{\"href\":\"/v2/plugins/mail/user-config\"}")
        .contains("\"language\":\"vogon\"")
        .contains("\"summarizeMails\":true")
        .contains("\"summaryFrequency\":\"HOURS_2\"")
        .contains("\"summarizeByEntity\":true")
        .contains("\"excludedTopics\":[{\"category\":{\"name\":\"hitchhiker\"},\"name\":\"towel\"}]");
    }

    @Test
    void shouldStoreConfigurationForUser() throws URISyntaxException, JsonProcessingException {
      ArgumentCaptor<UserMailConfiguration> configCaptor = ArgumentCaptor.forClass(UserMailConfiguration.class);

      UserMailConfigurationDto userMailConfigurationDto = new UserMailConfigurationDto();
      userMailConfigurationDto.setLanguage("vogon");
      userMailConfigurationDto.setSummarizeMails(true);
      userMailConfigurationDto.setSummaryFrequency(SummaryFrequency.HOURS_2);
      userMailConfigurationDto.setSummarizeByEntity(true);
      userMailConfigurationDto.setExcludedTopics(singleton(new TopicDto(new CategoryDto("hitchhiker"), "towel")));
      doNothing().when(context).store(eq("dent"), configCaptor.capture());

      dispatcher.invoke(
        create("PUT", "/v2/plugins/mail/user-config")
          .contentType("application/json")
          .content(mapper.writeValueAsBytes(userMailConfigurationDto)),
        response);

      assertThat(response.getStatus()).isEqualTo(204);
      assertThat(configCaptor.getValue().getExcludedTopics()).containsExactly(new Topic(new Category("hitchhiker"), "towel"));
      assertThat(configCaptor.getValue().getLanguage()).isEqualTo("vogon");
      assertThat(configCaptor.getValue().isSummarizeMails()).isEqualTo(true);
      assertThat(configCaptor.getValue().getSummaryFrequency()).isEqualTo(SummaryFrequency.HOURS_2);
      assertThat(configCaptor.getValue().isSummarizeByEntity()).isEqualTo(true);
    }

    @Test
    void shouldGetMailConfigurationWithMaskedPassword() throws URISyntaxException, UnsupportedEncodingException {
      when(context.getConfiguration())
        .thenReturn(new MailConfiguration("http://hog,org/", 25, ScmTransportStrategy.SMTPS, "marvin@hog.org", "marvin", "hitchhike", "SCM"));

      dispatcher.invoke(create("GET", "/v2/plugins/mail/config"), response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getContentAsString())
        .contains("\"username\":\"marvin\"")
        .contains("\"password\":\"__DUMMY__\"")
        .contains("\"host\":\"http://hog,org/\"")
        .contains("\"port\":25")
        .contains("\"from\":\"marvin@hog.org\"")
        .contains("\"subjectPrefix\":\"SCM\"")
        .contains("\"_links\":{\"self\":{\"href\":\"/v2/plugins/mail/config\"}")
        .contains("\"transportStrategy\":\"SMTPS\"");
    }

    @Test
    void shouldSetMailConfiguration() throws URISyntaxException {
      dispatcher.invoke(
        create("POST", "/v2/plugins/mail/config")
          .contentType("application/json")
          .content(("{" +
            "\"host\":\"http://hog,org/\"," +
            "\"port\":25," +
            "\"transportStrategy\":\"SMTPS\"," +
            "\"username\":\"marvin\"," +
            "\"password\":\"hitchhike\"," +
            "\"from\":\"marvin@hog.org\"," +
            "\"subjectPrefix\":\"SCM\"," +
            "\"language\":\"DE\"" +
            "}").getBytes()),
        response);

      assertThat(response.getStatus()).isEqualTo(204);

      verify(context).store(argThat(
        configuration -> {
          assertThat(configuration.getHost()).isEqualTo("http://hog,org/");
          assertThat(configuration.getUsername()).isEqualTo("marvin");
          assertThat(configuration.getPassword()).isEqualTo("hitchhike");
          assertThat(configuration.getTransportStrategy()).isEqualTo(ScmTransportStrategy.SMTPS);
          assertThat(configuration.getFrom()).isEqualTo("marvin@hog.org");
          assertThat(configuration.getSubjectPrefix()).isEqualTo("SCM");
          assertThat(configuration.getLanguage()).isEqualTo("DE");
          assertThat(configuration.getPort()).isEqualTo(25);
          return true;
        }
      ));
    }

    @Test
    void shouldSetMailConfigurationWithKeepingOldPassword() throws URISyntaxException {
      when(context.getConfiguration())
        .thenReturn(new MailConfiguration("old/", 0, ScmTransportStrategy.SMTP, "old", "old", "hitchhike", "old"));

      dispatcher.invoke(
        create("POST", "/v2/plugins/mail/config")
          .contentType("application/json")
          .content(("{" +
            "\"host\":\"http://hog,org/\"," +
            "\"port\":25," +
            "\"transportStrategy\":\"SMTPS\"," +
            "\"username\":\"marvin\"," +
            "\"password\":\"__DUMMY__\"," +
            "\"from\":\"marvin@hog.org\"," +
            "\"subjectPrefix\":\"SCM\"," +
            "\"language\":\"DE\"" +
            "}").getBytes()),
        response);

      assertThat(response.getStatus()).isEqualTo(204);

      verify(context).store(argThat(
        configuration -> {
          assertThat(configuration.getHost()).isEqualTo("http://hog,org/");
          assertThat(configuration.getUsername()).isEqualTo("marvin");
          assertThat(configuration.getPassword()).isEqualTo("hitchhike");
          assertThat(configuration.getTransportStrategy()).isEqualTo(ScmTransportStrategy.SMTPS);
          assertThat(configuration.getFrom()).isEqualTo("marvin@hog.org");
          assertThat(configuration.getSubjectPrefix()).isEqualTo("SCM");
          assertThat(configuration.getLanguage()).isEqualTo("DE");
          assertThat(configuration.getPort()).isEqualTo(25);
          return true;
        }
      ));
    }
  }

  @Nested
  class withAnonymousUser {
    @BeforeEach
    void setupUser() {
      Subject subject = mock(Subject.class);
      when(subject.getPrincipal()).thenReturn(USER_ANONYMOUS);
      ThreadContext.bind(subject);
    }

    @Test
    void shouldThrowUnauthorizedExceptionIfAnonymousUserGetsMailConfig() throws URISyntaxException, UnsupportedEncodingException {
      dispatcher.invoke(create("GET", "/v2/plugins/mail/user-config"), response);

      assertThat(response.getStatus()).isEqualTo(403);
      assertThat(response.getContentAsString()).isEqualTo("anonymous may not read the mail configuration");
    }

    @Test
    void shouldThrowUnauthorizedExceptionIfAnonymousUserChangesMailConfig() throws URISyntaxException, UnsupportedEncodingException {
      dispatcher.invoke(create("PUT", "/v2/plugins/mail/user-config")
        .contentType("application/json")
        .content(("{\"" +
          "language\":\"vogon\"," +
          "\"excludedTopics\":[{\"category\":{\"name\":\"hitchhiker\"},\"name\":\"towel\"}]" +
          "}").getBytes()), response);

      assertThat(response.getStatus()).isEqualTo(403);
      assertThat(response.getContentAsString()).isEqualTo("anonymous may not change the mail configuration");
    }
  }
}
