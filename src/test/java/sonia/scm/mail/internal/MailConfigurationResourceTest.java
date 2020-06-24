/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.mail.internal;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.mail.api.Category;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailConfigurationResourceTest {

  @Mock
  MailService mailService;
  @Mock
  MailContext context;

  RestDispatcher dispatcher = new RestDispatcher();

  MockHttpResponse response = new MockHttpResponse();

  @BeforeEach
  void setupResource() {
    MailConfigurationMapperImpl mapper = new MailConfigurationMapperImpl();
    MailConfigurationResource resource = new MailConfigurationResource(mailService, context, mapper);
    dispatcher.addSingletonResource(resource);
  }

  @BeforeEach
  void setupUser() {
    Subject subject = mock(Subject.class);
    PrincipalCollection principalCollection = mock(PrincipalCollection.class);
    lenient().when(principalCollection.oneByType(User.class)).thenReturn(new User("dent"));
    lenient().when(subject.getPrincipals()).thenReturn(principalCollection);
    ThreadContext.bind(subject);
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
    userMailConfiguration.setExcludedTopics(singleton(new Topic(new Category("hitchhiker"), "towel")));
    when(context.getUserConfiguration("dent")).thenReturn(of(userMailConfiguration));

    dispatcher.invoke(create("GET", "/v2/plugins/mail/user-config"), response);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString())
      .contains("\"self\":{\"href\":\"/v2/plugins/mail/user-config\"}")
      .contains("\"update\":{\"href\":\"/v2/plugins/mail/user-config\"}")
      .contains("\"language\":\"vogon\"")
      .contains("\"excludedTopics\":[{\"category\":{\"name\":\"hitchhiker\"},\"name\":\"towel\"}]");
  }

  @Test
  void shouldStoreConfigurationForUser() throws URISyntaxException {
    ArgumentCaptor<UserMailConfiguration> configCaptor = ArgumentCaptor.forClass(UserMailConfiguration.class);

    UserMailConfigurationDto userMailConfigurationDto = new UserMailConfigurationDto();
    userMailConfigurationDto.setLanguage("vogon");
    userMailConfigurationDto.setExcludedTopics(singleton(new TopicDto(new CategoryDto("hitchhiker"), "towel")));
    doNothing().when(context).store(eq("dent"), configCaptor.capture());

    dispatcher.invoke(
      create("PUT", "/v2/plugins/mail/user-config")
        .contentType("application/json")
        .content(("{\"" +
          "language\":\"vogon\"," +
          "\"excludedTopics\":[{\"category\":{\"name\":\"hitchhiker\"},\"name\":\"towel\"}]" +
          "}").getBytes()),
      response);

    assertThat(response.getStatus()).isEqualTo(204);
    assertThat(configCaptor.getValue().getExcludedTopics()).containsExactly(new Topic(new Category("hitchhiker"), "towel"));
    assertThat(configCaptor.getValue().getLanguage()).isEqualTo("vogon");
  }
}
